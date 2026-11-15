package fr.esgi.banque.service.impl;

import fr.esgi.banque.service.VirementRefuseException;
import java.time.DayOfWeek;
import fr.esgi.banque.service.CompteIntrouvableException;
import fr.esgi.banque.service.CompteRepository;
import fr.esgi.banque.service.NotificationService;
import fr.esgi.banque.business.Compte;
import fr.esgi.banque.business.Virement;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.Clock;

public class VirementServiceImpl {

    private final CompteRepository repository;
    private final NotificationService notifications;
    private final Clock horloge;

    private static final BigDecimal SEUIL_ALERTE = new BigDecimal("10000");

    public VirementServiceImpl(CompteRepository repository, NotificationService notifications, Clock horloge) {
        this.repository = repository;
        this.notifications = notifications;
        this.horloge = horloge;
    }

    public Virement virer(String numeroSource, String numeroDestination, BigDecimal montant) {
        verifierParametres(numeroSource, numeroDestination, montant);

        verifierJourAutorise();

        Compte source = trouverCompte(numeroSource);
        Compte destination = trouverCompte(numeroDestination);

        source.debiter(montant);
        destination.crediter(montant);

        repository.sauvegarder(source);
        repository.sauvegarder(destination);

        alerterSiMontantImportant(source, montant);

        return new Virement(numeroSource, numeroDestination, montant, LocalDate.now(horloge));
    }

    private void verifierParametres(String numeroSource, String numeroDestination, BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Le montant du virement doit être strictement positif");
        }
        if (numeroSource.equals(numeroDestination)) {
            throw new IllegalArgumentException("Un virement ne peut pas être effectué vers le même compte");
        }
    }

    private Compte trouverCompte(String numero) {
        return repository.trouver(numero).orElseThrow(() -> new CompteIntrouvableException((numero)));
    }

    private void alerterSiMontantImportant(Compte source, BigDecimal montant) {
        if (montant.compareTo(SEUIL_ALERTE) > 0) {
            notifications.alerter(source.getTitulaire(), "Virement important : " + montant + " €");
        }
    }

    private void verifierJourAutorise() {
        if (LocalDate.now(horloge).getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new VirementRefuseException("Les virements sont refusés le dimanche");
        }
    }
}
