package fr.esgi.banque.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

/**
 * Test d'exemple fourni pour vérifier que le projet est bien configuré.
 * À compléter (partie A).
 */
class CompteTest {

	@Test
	void shouldIncreaseBalanceWhenCrediting() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		//Act
		compte.crediter(new BigDecimal("50.00"));

		//Assert
		assertThat(compte.getSolde()).isEqualByComparingTo("150.00");
	}

	// C1 : Le numéro et le titulaire sont obligatoires (ni null, ni vides). Sinon IllegalArgumentException.

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("Vérifier numéro si vide ou null retourne une exception")
	void VerifierNumeroSiVideOuNullRetourneUneException(String numero) {
		assertThrows(IllegalArgumentException.class, () ->
				new Compte(numero, "Alice", new BigDecimal("100.00"), new BigDecimal("200"))
		);

	}

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("Vérifier titulaire si vide ou null retourne une exception")
	void VerifierTitulaireSiVideOuNullRetourneUneException(String titulaire) {
		assertThrows(IllegalArgumentException.class, () ->
				new Compte("FR001", titulaire, new BigDecimal("100.00"), new BigDecimal("200"))
		);
	}

	// C2 : Le solde initial et le découvert autorisé sont obligatoires. Le découvert autorisé est positif ou nul. Sinon IllegalArgumentException.

	@Test
	@DisplayName("Vérifier solde si Null retourne une exception")
	void VerifierSoldeSiNullRetourneUneException() {
		assertThrows(IllegalArgumentException.class, () ->
				new Compte("FR001", "Alice", null, new BigDecimal("200"))
		);
	}

	@Test
	@DisplayName("Vérifier le découvert si Null retourne une exception")
	void VerifierLeDecouvertSiNullRetourneUneException() {
		assertThrows(IllegalArgumentException.class, () ->
				new Compte("FR001", "Alice", new BigDecimal("100.00"), null)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "100.00"})
	@DisplayName("Vérifier le découvert si il est à 0 ou positif alors ne retourne pas d'erreur")
	void VerifierLeDecouvertSiEst0RetournePasDerreur(String valeur) {
		//Arrange
		BigDecimal decouvertAutoriser = new BigDecimal(valeur);

		//Act
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), decouvertAutoriser);

		//Assert
		assertThat(compte.getDecouvertAutorise()).isEqualByComparingTo(decouvertAutoriser);
	}

	@ParameterizedTest
	@ValueSource(strings = {"-0.01", "-100.00"})
	@DisplayName("Refuser un découvert autorisé négatif")
	void verifierDecouvertAutoriseNegatifRetourneUneException(String decouvert) {
		//Arrange
		BigDecimal decouvertAutorise = new BigDecimal(decouvert);

		//Act / Assert
		assertThrows(IllegalArgumentException.class, () ->
				new Compte("FR001", "Alice", new BigDecimal("100.00"), decouvertAutorise));
	}

	// C3	crediter n'accepte qu'un montant strictement positif. Sinon IllegalArgumentException.

	@Test
	@DisplayName("Vérifier si un montant créditer est négatif retourne une erreur")
	void VerifierSiMontantCrediterNegatifRetourneUneErreur() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));
		BigDecimal montantACrediter = new BigDecimal("-10.00");

		//Assert
		assertThrows(IllegalArgumentException.class, () -> compte.crediter(montantACrediter));
	}

	@Test
	@DisplayName("Vérifier si un montant créditer est positif ne retourne pas une erreur")
	void VerifierSiMontantCrediterPositifNeRetournePasDerreur() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));
		BigDecimal montantACrediter = new BigDecimal("10.00");

		//Act
		compte.crediter(montantACrediter);

		//Assert
		assertThat(compte.getSolde()).isEqualByComparingTo(new BigDecimal("110.00"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "0.00"})
	@DisplayName("Refuser un crédit de zéro quelle que soit son écriture")
	void verifierCreditDeZeroRetourneUneException(String montant) {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		//Act / Assert
		assertThrows(IllegalArgumentException.class, () -> compte.crediter(new BigDecimal(montant)));
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
	}

	@Test
	@DisplayName("Refuser un crédit null sans modifier le solde")
	void verifierCreditNullRetourneUneException() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		//Act / Assert
		assertThrows(IllegalArgumentException.class, () -> compte.crediter(null));
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
	}

	// C4	debiter n'accepte qu'un montant strictement positif. Sinon IllegalArgumentException.

	@Test
	@DisplayName("Vérifier si un montant débiter est négatif retourne une erreur")
	void VerifierSiMontantDebiterNegatifRetourneUneErreur() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		//Assert
		assertThrows(IllegalArgumentException.class, () -> compte.debiter(new BigDecimal("-10.00")));
	}

	@Test
	@DisplayName("Vérifier si un montant débiter est positif ne retourne pas une erreur")
	void VerifierSiMontantDebiterPositifNeRetournePasUneErreur() {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));

		//Act
		compte.debiter(new BigDecimal("10.00"));

		//Assert
		assertThat(compte.getSolde()).isEqualByComparingTo(new BigDecimal("90.00"));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"0", "0.00"})
	@DisplayName("Refuser un débit null ou égal à zéro sans modifier le solde")
	void verifierDebitNullOuZeroRetourneUneException(String montant) {
		//Arrange
		Compte compte = new Compte("FR001", "Alice", new BigDecimal("100.00"), new BigDecimal("200"));
		BigDecimal montantADebiter = montant == null ? null : new BigDecimal(montant);

		//Act / Assert
		assertThrows(IllegalArgumentException.class, () -> compte.debiter(montantADebiter));
		assertThat(compte.getSolde()).isEqualByComparingTo("100.00");
	}

}
