package com.felixbrucker.currencyconverter.data

import com.felixbrucker.currencyconverter.model.Currency

object CurrenciesCatalog {

    val allCurrencies: List<Currency> = listOf(
        // Popular Fiat
        Currency("USD", "United States Dollar", "$", "🇺🇸", false, "United States", 2, true),
        Currency("EUR", "Euro", "€", "🇪🇺", false, "European Union", 2, true),
        Currency("GBP", "British Pound Sterling", "£", "🇬🇧", false, "United Kingdom", 2, true),
        Currency("NZD", "New Zealand Dollar", "$", "🇳🇿", false, "New Zealand", 2, true),
        Currency("AUD", "Australian Dollar", "$", "🇦🇺", false, "Australia", 2, true),
        Currency("CAD", "Canadian Dollar", "$", "🇨🇦", false, "Canada", 2, true),
        Currency("JPY", "Japanese Yen", "¥", "🇯🇵", false, "Japan", 0, true),
        Currency("CHF", "Swiss Franc", "CHF", "🇨🇭", false, "Switzerland", 2, true),
        Currency("CNY", "Chinese Yuan", "¥", "🇨🇳", false, "China", 2, true),
        Currency("INR", "Indian Rupee", "₹", "🇮🇳", false, "India", 2, true),
        Currency("SGD", "Singapore Dollar", "$", "🇸🇬", false, "Singapore", 2, true),
        Currency("HKD", "Hong Kong Dollar", "$", "🇭🇰", false, "Hong Kong", 2, true),
        Currency("BRL", "Brazilian Real", "R$", "🇧🇷", false, "Brazil", 2, true),
        Currency("KRW", "South Korean Won", "₩", "🇰🇷", false, "South Korea", 0, true),
        Currency("MXN", "Mexican Peso", "$", "🇲🇽", false, "Mexico", 2, true),
        Currency("SEK", "Swedish Krona", "kr", "🇸🇪", false, "Sweden", 2, true),
        Currency("NOK", "Norwegian Krone", "kr", "🇳🇴", false, "Norway", 2, true),
        Currency("DKK", "Danish Krone", "kr", "🇩🇰", false, "Denmark", 2, true),
        Currency("PLN", "Polish Zloty", "zł", "🇵🇱", false, "Poland", 2, true),
        Currency("TRY", "Turkish Lira", "₺", "🇹🇷", false, "Turkey", 2, true),
        Currency("THB", "Thai Baht", "฿", "🇹🇭", false, "Thailand", 2, true),
        Currency("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", false, "Indonesia", 0, true),
        Currency("ZAR", "South African Rand", "R", "🇿🇦", false, "South Africa", 2, true),
        Currency("AED", "United Arab Emirates Dirham", "د.إ", "🇦🇪", false, "United Arab Emirates", 2, true),
        Currency("SAR", "Saudi Riyal", "﷼", "🇸🇦", false, "Saudi Arabia", 2, true),
        Currency("TWD", "New Taiwan Dollar", "NT$", "🇹🇼", false, "Taiwan", 2, false),
        Currency("PHP", "Philippine Peso", "₱", "🇵🇭", false, "Philippines", 2, false),
        Currency("MYR", "Malaysian Ringgit", "RM", "🇲🇾", false, "Malaysia", 2, false),
        Currency("VND", "Vietnamese Dong", "₫", "🇻🇳", false, "Vietnam", 0, false),
        Currency("ILS", "Israeli New Shekel", "₪", "🇮🇱", false, "Israel", 2, false),
        Currency("CLP", "Chilean Peso", "$", "🇨🇱", false, "Chile", 0, false),
        Currency("COP", "Colombian Peso", "$", "🇨🇴", false, "Colombia", 0, false),
        Currency("ARS", "Argentine Peso", "$", "🇦🇷", false, "Argentina", 2, false),
        Currency("CZK", "Czech Koruna", "Kč", "🇨🇿", false, "Czech Republic", 2, false),
        Currency("HUF", "Hungarian Forint", "Ft", "🇭🇺", false, "Hungary", 0, false),
        Currency("RON", "Romanian Leu", "lei", "🇷🇴", false, "Romania", 2, false),
        Currency("BGN", "Bulgarian Lev", "лв", "🇧🇬", false, "Bulgaria", 2, false),
        Currency("EGP", "Egyptian Pound", "E£", "🇪🇬", false, "Egypt", 2, false),
        Currency("NGN", "Nigerian Naira", "₦", "🇳🇬", false, "Nigeria", 2, false),
        Currency("KES", "Kenyan Shilling", "KSh", "🇰🇪", false, "Kenya", 2, false),
        Currency("GHS", "Ghanaian Cedi", "GH₵", "🇬🇭", false, "Ghana", 2, false),
        Currency("PKR", "Pakistani Rupee", "₨", "🇵🇰", false, "Pakistan", 2, false),
        Currency("BDT", "Bangladeshi Taka", "৳", "🇧🇩", false, "Bangladesh", 2, false),
        Currency("QAR", "Qatari Rial", "﷼", "🇶🇦", false, "Qatar", 2, false),
        Currency("KWD", "Kuwaiti Dinar", "KD", "🇰🇼", false, "Kuwait", 3, false),
        Currency("BHD", "Bahraini Dinar", "BD", "🇧🇭", false, "Bahrain", 3, false),
        Currency("OMR", "Omani Rial", "﷼", "🇴🇲", false, "Oman", 3, false),
        Currency("JOD", "Jordanian Dinar", "JD", "🇯🇴", false, "Jordan", 3, false),
        Currency("MAD", "Moroccan Dirham", "DH", "🇲🇦", false, "Morocco", 2, false),
        Currency("PEN", "Peruvian Sol", "S/.", "🇵🇪", false, "Peru", 2, false),
        Currency("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦", false, "Ukraine", 2, false),
        Currency("ISK", "Icelandic Krona", "kr", "🇮🇸", false, "Iceland", 0, false),
        Currency("HRK", "Croatian Kuna", "kn", "🇭🇷", false, "Croatia", 2, false),
        Currency("RSD", "Serbian Dinar", "din", "🇷🇸", false, "Serbia", 2, false),
        Currency("CRC", "Costa Rican Colon", "₡", "🇨🇷", false, "Costa Rica", 2, false),
        Currency("DOP", "Dominican Peso", "RD$", "🇩🇴", false, "Dominican Republic", 2, false),
        Currency("UYU", "Uruguayan Peso", "\$U", "🇺🇾", false, "Uruguay", 2, false),
        Currency("KZT", "Kazakhstani Tenge", "₸", "🇰🇿", false, "Kazakhstan", 2, false),
        Currency("LKR", "Sri Lankan Rupee", "Rs", "🇱🇰", false, "Sri Lanka", 2, false),

        // Crypto Currencies
        Currency("BTC", "Bitcoin", "₿", "🪙", true, "Decentralized", 6, true),
        Currency("ETH", "Ethereum", "Ξ", "🔷", true, "Decentralized", 6, true),
        Currency("SOL", "Solana", "◎", "🟣", true, "Solana Network", 4, true),
        Currency("BNB", "Binance Coin", "BNB", "🟡", true, "BNB Chain", 4, true),
        Currency("XRP", "XRP / Ripple", "XRP", "✕", true, "Ripple", 4, true),
        Currency("DOGE", "Dogecoin", "Ð", "🐕", true, "Decentralized", 2, true),
        Currency("ADA", "Cardano", "₳", "🔵", true, "Cardano", 4, true),
        Currency("AVAX", "Avalanche", "AVAX", "🔺", true, "Avalanche", 4, false),
        Currency("LINK", "Chainlink", "LINK", "🔗", true, "Ethereum", 4, false),
        Currency("DOT", "Polkadot", "DOT", "⚪", true, "Polkadot", 4, false),
        Currency("MATIC", "Polygon", "POL", "🟣", true, "Polygon", 4, false),
        Currency("NEAR", "NEAR Protocol", "NEAR", "🟢", true, "NEAR", 4, false),
        Currency("LTC", "Litecoin", "Ł", "🪙", true, "Decentralized", 4, false),
        Currency("BCH", "Bitcoin Cash", "BCH", "🟢", true, "Decentralized", 4, false),
        Currency("SHIB", "Shiba Inu", "SHIB", "🐶", true, "Ethereum", 0, false),
        Currency("UNI", "Uniswap", "UNI", "🦄", true, "Ethereum", 4, false),
        Currency("TRX", "TRON", "TRX", "🔴", true, "TRON", 4, false),
        Currency("XLM", "Stellar Lumens", "XLM", "🚀", true, "Stellar", 4, false),
        Currency("ATOM", "Cosmos", "ATOM", "⚛️", true, "Cosmos", 4, false),
        Currency("SUI", "Sui", "SUI", "💧", true, "Sui Network", 4, false),
        Currency("TON", "Toncoin", "TON", "💎", true, "TON", 4, false)
    )

    private val currencyMap = allCurrencies.associateBy { it.code }

    fun find(code: String): Currency? = currencyMap[code.uppercase()]

    val defaultSelectedCodes = listOf("USD", "EUR")
}
