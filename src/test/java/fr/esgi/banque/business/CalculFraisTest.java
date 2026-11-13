package fr.esgi.banque.business;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculFraisTest {
    // F1	Le titulaire doit avoir 18 ans ou plus, et le solde moyen est obligatoire. Sinon IllegalArgumentException.

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 15, 17})
    @DisplayName("Vérifier qu'un titulaire mineur déclenche une exception")
    void verifierAgeMineurRetourneUneException(int age) {
        //Arrange
        CalculFrais calculFrais = new CalculFrais();

        //Assert
        assertThrows(IllegalArgumentException.class, () -> calculFrais.fraisMensuels(age, new BigDecimal("500.00"), false));
    }

    @ParameterizedTest
    @ValueSource(ints = {18, 20, 45, 87})
    @DisplayName("Vérifier qu'un titulaire majeur est accepté")
    void VerifierAgeMajeurNeRetournePasUneException(int age) {
        //Arrange
        CalculFrais calculFrais = new CalculFrais();

        //Assert
        assertDoesNotThrow(() -> calculFrais.fraisMensuels(age, new BigDecimal("500.00"), false));
    }

    @Test
    @DisplayName("Vérifier que le solde moyen null déclenche une exception")
    void VerifierSoldeMoyenNullRetourneUneException() {
        //Arrange
        CalculFrais calculFrais = new CalculFrais();

        //Assert
        assertThrows(IllegalArgumentException.class, () -> calculFrais.fraisMensuels( 30, null, false));
    }

    // F2	Les frais de tenue de compte sont de 2,00 € par mois.

    @Test
    @DisplayName("Vérifier que les frais de compte sont à 2€")
    void VerifierFraisDeCompteA2Euros() {
        //Arrange
        CalculFrais calculFrais = new CalculFrais();

        //Act
        BigDecimal frais = calculFrais.fraisMensuels(30, new BigDecimal("500.00"), false);

        //Assert
        assertThat(frais).isEqualByComparingTo(new BigDecimal("2.00"));
    }

    // F3	Les frais de tenue sont offerts aux titulaires de moins de 26 ans.

    @ParameterizedTest
    @CsvSource({
            "18, 0.00",
            "25, 0.00",
            "26, 2.00",
            "27, 2.00"
    })
    @DisplayName("Vérifier que les frais de compte sont gratuit pour les moins de 26 ans")
    void VerifierFraisDeCompteGratuitPourMoinsDe26Ans(int age, String fraisAttendus) {
        //Arrange
        CalculFrais calculFrais = new CalculFrais();

        //Act
        BigDecimal frais = calculFrais.fraisMensuels(age, new BigDecimal("500.00"), false);

        //Assert
        assertThat(frais).isEqualByComparingTo(fraisAttendus);
    }

}
