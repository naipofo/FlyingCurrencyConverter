import data.util.roundCurrency
import org.junit.jupiter.api.Test

class CurrencyRounding {
    @Test
    fun `Adding commas to whole numbers`() {
        assert(0.0.roundCurrency() == "0.00")
        assert(3.0.roundCurrency() == "3.00")
        assert((-2.0).roundCurrency() == "-2.00")
    }

    @Test
    fun `Adding last zero`() {
        assert(0.1.roundCurrency() == "0.10")
        assert(41.1.roundCurrency() == "41.10")
    }

    @Test
    fun `Removing precision`() {
        assert(0.12121.roundCurrency() == "0.12")
        assert(0.129.roundCurrency() == "0.13")
    }
}
