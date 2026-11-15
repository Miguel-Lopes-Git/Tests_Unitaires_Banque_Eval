package fr.esgi.banque.service.impl;

import fr.esgi.banque.business.SoldeInsuffisantException;
import fr.esgi.banque.business.Virement;
import java.time.LocalDate;
import fr.esgi.banque.business.Compte;
import fr.esgi.banque.service.CompteIntrouvableException;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class VirementServiceImplTest {

    private static final String NUMERO_SOURCE = "FR001";
    private static final String NUMERO_DESTINATION = "FR002";

    @Mock
    CompteRepository repository;

    @Mock
    NotificationService notifications;

    // 12h Vendredi 13 Novembre 2026
    Clock horloge = Clock.fixed(Instant.parse("2026-11-13T12:00:00Z"), ZoneOffset.UTC);

    VirementServiceImpl service;

    private void configurerComptesExistants(Compte source, Compte destination) {
        when(repository.trouver(source.getNumero())).thenReturn(Optional.of(source));

        when(repository.trouver(destination.getNumero())).thenReturn(Optional.of(destination));
    }

    @BeforeEach
    void CreerService() {
        service = new VirementServiceImpl(repository, notifications, horloge);
    }

    // V1	Un montant null, nul ou négatif est refusé : IllegalArgumentException.

    @ParameterizedTest(name = "Le virement de {0}€ est refusé")
    @DisplayName("Vérifier si un montant Null, Nul ou negatif retourne une exception")
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10.00"})
    void verifierMontantNullNulOuNegatifRetourneUneException(String valeur) {

        // Arrange
        BigDecimal montant = valeur == null ? null : new BigDecimal(valeur);

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant));
    }

    // V2	Un virement vers le même compte est refusé : IllegalArgumentException.

    @Test
    @DisplayName("Faire un virement vers le même compte est refusé")
    void VirementVersLeMemeCompteEstRefuse() {
        // Arrange
        BigDecimal montant = new BigDecimal("10");

        // Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.virer(NUMERO_SOURCE, NUMERO_SOURCE, montant));
    }

    // V3	Si l'un des deux comptes n'existe pas (trouver renvoie Optional.empty()) : CompteIntrouvableException.

    @Test
    @DisplayName("Faire un virement depuis un compte source inexistant retourne une exception")
    void VirementDepuisUnCompteSourceInexistantRetourneUneException() {
        // Arrange
        BigDecimal montant = new BigDecimal("10");
        when(repository.trouver(NUMERO_SOURCE)).thenReturn(Optional.empty());

        // Assert
        assertThrows(CompteIntrouvableException.class, () -> service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant));
    }

    @Test
    @DisplayName("Faire un virement vers un compte de destination inexistant retourne une exception")
    void VirementVersUnCompteDeDestinationInexistantRetourneUneException() {
        // Arrange
        BigDecimal montant = new BigDecimal("10");
        Compte source = new Compte(NUMERO_SOURCE, "Test", new BigDecimal("100"), new BigDecimal("200"));
        when(repository.trouver(NUMERO_SOURCE)).thenReturn(Optional.of(source));
        when(repository.trouver(NUMERO_DESTINATION)).thenReturn(Optional.empty());

        // Assert
        assertThrows(CompteIntrouvableException.class, () -> service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant));
    }

    // V4	Cas nominal : le compte source est débité, le compte destination est crédité, les deux comptes sont sauvegardés. Le Virement renvoyé contient la date du jour.


    @Test
    @DisplayName("Effectuer un virement et renvoyer ses informations")
    void verifierVirementDebiteCrediteSauvegardeEtRetourneLaDate() {
        // Arrange
        BigDecimal montant = new BigDecimal("10");

        Compte source = new Compte(NUMERO_SOURCE, "Test", new BigDecimal("100"), new BigDecimal("200"));
        Compte destination = new Compte(NUMERO_DESTINATION, "Test2", new BigDecimal("50"), BigDecimal.ZERO);

        configurerComptesExistants(source, destination);

        // Act
        Virement virement = service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant);

        // Assert
        assertThat(source.getSolde()).isEqualByComparingTo("90");
        assertThat(destination.getSolde()).isEqualByComparingTo("60");

        verify(repository).sauvegarder(source);
        verify(repository).sauvegarder(destination);

        assertEquals(new Virement(NUMERO_SOURCE, NUMERO_DESTINATION, montant, LocalDate.of(2026, 11, 13)), virement);
    }

    // V5	Si le solde de la source est insuffisant, la SoldeInsuffisantException remonte et rien n'est sauvegardé.

    @Test
    @DisplayName("Refuser un virement sans sauvegarde si le solde est insuffisant")
    void verifierSoldeInsuffisantRefuseVirementSansSauvegarde() {
        // Arrange
        BigDecimal montant = new BigDecimal("300.01");

        Compte source = new Compte(NUMERO_SOURCE, "Test", new BigDecimal("100"), new BigDecimal("200"));
        Compte destination = new Compte(NUMERO_DESTINATION, "Test2", new BigDecimal("50"), BigDecimal.ZERO);

        configurerComptesExistants(source, destination);

        // Act / Assert
        assertThrows(SoldeInsuffisantException.class, () -> service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant));

        verify(repository, never()).sauvegarder(any());

        assertThat(source.getSolde()).isEqualByComparingTo("100");
        assertThat(destination.getSolde()).isEqualByComparingTo("50");
    }

    // V6	Un virement strictement supérieur à 10 000 € déclenche une alerte au titulaire du compte source (NotificationService.alerter). Pas d'alerte sinon.

    @ParameterizedTest(name = "{0} € : {1} alerte attendue")
    @CsvSource({
            "9999.99, 0",
            "10000, 0",
            "10000.00, 0",
            "10000.01, 1",
            "15000, 1"
    })
    @DisplayName("Alerter le titulaire uniquement au-delà de 10 000 €")
    void verifierAlerteSelonMontantDuVirement(String valeur, int nombreAlertes) {
        // Arrange
        BigDecimal montant = new BigDecimal(valeur);

        Compte source = new Compte(NUMERO_SOURCE, "Test", new BigDecimal("20000"), BigDecimal.ZERO);
        Compte destination = new Compte(NUMERO_DESTINATION, "Test2", new BigDecimal("50"), BigDecimal.ZERO);

        configurerComptesExistants(source, destination);

        // Act
        service.virer(NUMERO_SOURCE, NUMERO_DESTINATION, montant);

        // Assert
        verify(notifications, times(nombreAlertes))
                .alerter(any(), any());

        if (nombreAlertes > 0) {
            verify(notifications).alerter(eq(source.getTitulaire()), anyString());
        }
    }

}