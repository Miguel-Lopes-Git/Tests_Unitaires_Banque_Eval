package fr.esgi.banque.service.impl;

import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class VirementServiceImplTest {

    @Mock
    CompteRepository repository;

    @Mock
    NotificationService notifications;

    // 12h Vendredi 13 Novembre 2026
    Clock horloge = Clock.fixed(Instant.parse("2026-11-13T12:00:00Z"), ZoneOffset.UTC);

    VirementServiceImpl service;

    @BeforeEach
    void CreerService() {
        service = new VirementServiceImpl(repository, notifications, horloge);
    }

    // V1	Un montant null, nul ou négatif est refusé : IllegalArgumentException.

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10.00"})
    void verifierMontantNullNulOuNegatifRetourneUneException(String valeur) {

        // Vaut null ou un nombre
        BigDecimal montant = valeur == null ? null : new BigDecimal(valeur);

        //Act / Assert
        assertThrows(IllegalArgumentException.class, () -> service.virer("FR001", "FR002", montant));
    }
}