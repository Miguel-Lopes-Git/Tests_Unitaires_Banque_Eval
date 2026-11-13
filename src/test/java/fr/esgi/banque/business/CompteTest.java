package fr.esgi.banque.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
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

}
