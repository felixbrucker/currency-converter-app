package com.felixbrucker.currencyconverter.data

import com.felixbrucker.currencyconverter.model.Currency

object CurrenciesCatalog {

    val allCurrencies: List<Currency> = listOf(
        // Popular Fiat
        Currency(
            "USD",
            "United States Dollar",
            "$",
            "🇺🇸",
            country = "United States",
            decimalPlaces = 2
        ),
        Currency("EUR", "Euro", "€", "🇪🇺", country = "European Union", decimalPlaces = 2),
        Currency(
            "GBP",
            "British Pound Sterling",
            "£",
            "🇬🇧",
            country = "United Kingdom",
            decimalPlaces = 2
        ),
        Currency("NZD", "New Zealand Dollar", "$", "🇳🇿", country = "New Zealand", decimalPlaces = 2),
        Currency("AUD", "Australian Dollar", "$", "🇦🇺", country = "Australia", decimalPlaces = 2),
        Currency("CAD", "Canadian Dollar", "$", "🇨🇦", country = "Canada", decimalPlaces = 2),
        Currency("JPY", "Japanese Yen", "¥", "🇯🇵", country = "Japan", decimalPlaces = 0),
        Currency("CHF", "Swiss Franc", "CHF", "🇨🇭", country = "Switzerland", decimalPlaces = 2),
        Currency("CNY", "Chinese Yuan", "¥", "🇨🇳", country = "China", decimalPlaces = 2),
        Currency("INR", "Indian Rupee", "₹", "🇮🇳", country = "India", decimalPlaces = 2),
        Currency("SGD", "Singapore Dollar", "$", "🇸🇬", country = "Singapore", decimalPlaces = 2),
        Currency("HKD", "Hong Kong Dollar", "$", "🇭🇰", country = "Hong Kong", decimalPlaces = 2),
        Currency("BRL", "Brazilian Real", "R$", "🇧🇷", country = "Brazil", decimalPlaces = 2),
        Currency("KRW", "South Korean Won", "₩", "🇰🇷", country = "South Korea", decimalPlaces = 0),
        Currency("MXN", "Mexican Peso", "$", "🇲🇽", country = "Mexico", decimalPlaces = 2),
        Currency("SEK", "Swedish Krona", "kr", "🇸🇪", country = "Sweden", decimalPlaces = 2),
        Currency("NOK", "Norwegian Krone", "kr", "🇳🇴", country = "Norway", decimalPlaces = 2),
        Currency("DKK", "Danish Krone", "kr", "🇩🇰", country = "Denmark", decimalPlaces = 2),
        Currency("PLN", "Polish Zloty", "zł", "🇵🇱", country = "Poland", decimalPlaces = 2),
        Currency("TRY", "Turkish Lira", "₺", "🇹🇷", country = "Turkey", decimalPlaces = 2),
        Currency("THB", "Thai Baht", "฿", "🇹🇭", country = "Thailand", decimalPlaces = 2),
        Currency("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", country = "Indonesia", decimalPlaces = 0),
        Currency(
            "ZAR",
            "South African Rand",
            "R",
            "🇿🇦",
            country = "South Africa",
            decimalPlaces = 2
        ),
        Currency(
            "AED",
            "United Arab Emirates Dirham",
            "د.إ",
            "🇦🇪",
            country = "United Arab Emirates",
            decimalPlaces = 2
        ),
        Currency("SAR", "Saudi Riyal", "﷼", "🇸🇦", country = "Saudi Arabia", decimalPlaces = 2),
        Currency("TWD", "New Taiwan Dollar", "NT$", "🇹🇼", country = "Taiwan", decimalPlaces = 2),
        Currency("PHP", "Philippine Peso", "₱", "🇵🇭", country = "Philippines", decimalPlaces = 2),
        Currency("MYR", "Malaysian Ringgit", "RM", "🇲🇾", country = "Malaysia", decimalPlaces = 2),
        Currency("VND", "Vietnamese Dong", "₫", "🇻🇳", country = "Vietnam", decimalPlaces = 0),
        Currency("ILS", "Israeli New Shekel", "₪", "🇮🇱", country = "Israel", decimalPlaces = 2),
        Currency("CLP", "Chilean Peso", "$", "🇨🇱", country = "Chile", decimalPlaces = 0),
        Currency("COP", "Colombian Peso", "$", "🇨🇴", country = "Colombia", decimalPlaces = 0),
        Currency("ARS", "Argentine Peso", "$", "🇦🇷", country = "Argentina", decimalPlaces = 2),
        Currency("CZK", "Czech Koruna", "Kč", "🇨🇿", country = "Czech Republic", decimalPlaces = 2),
        Currency("HUF", "Hungarian Forint", "Ft", "🇭🇺", country = "Hungary", decimalPlaces = 0),
        Currency("RON", "Romanian Leu", "lei", "🇷🇴", country = "Romania", decimalPlaces = 2),
        Currency("BGN", "Bulgarian Lev", "лв", "🇧🇬", country = "Bulgaria", decimalPlaces = 2),
        Currency("EGP", "Egyptian Pound", "E£", "🇪🇬", country = "Egypt", decimalPlaces = 2),
        Currency("NGN", "Nigerian Naira", "₦", "🇳🇬", country = "Nigeria", decimalPlaces = 2),
        Currency("KES", "Kenyan Shilling", "KSh", "🇰🇪", country = "Kenya", decimalPlaces = 2),
        Currency("GHS", "Ghanaian Cedi", "GH₵", "🇬🇭", country = "Ghana", decimalPlaces = 2),
        Currency("PKR", "Pakistani Rupee", "₨", "🇵🇰", country = "Pakistan", decimalPlaces = 2),
        Currency("BDT", "Bangladeshi Taka", "৳", "🇧🇩", country = "Bangladesh", decimalPlaces = 2),
        Currency("QAR", "Qatari Rial", "﷼", "🇶🇦", country = "Qatar", decimalPlaces = 2),
        Currency("KWD", "Kuwaiti Dinar", "KD", "🇰🇼", country = "Kuwait", decimalPlaces = 3),
        Currency("BHD", "Bahraini Dinar", "BD", "🇧🇭", country = "Bahrain", decimalPlaces = 3),
        Currency("OMR", "Omani Rial", "﷼", "🇴🇲", country = "Oman", decimalPlaces = 3),
        Currency("JOD", "Jordanian Dinar", "JD", "🇯🇴", country = "Jordan", decimalPlaces = 3),
        Currency("MAD", "Moroccan Dirham", "DH", "🇲🇦", country = "Morocco", decimalPlaces = 2),
        Currency("PEN", "Peruvian Sol", "S/.", "🇵🇪", country = "Peru", decimalPlaces = 2),
        Currency("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦", country = "Ukraine", decimalPlaces = 2),
        Currency("ISK", "Icelandic Krona", "kr", "🇮🇸", country = "Iceland", decimalPlaces = 0),
        Currency("HRK", "Croatian Kuna", "kn", "🇭🇷", country = "Croatia", decimalPlaces = 2),
        Currency("RSD", "Serbian Dinar", "din", "🇷🇸", country = "Serbia", decimalPlaces = 2),
        Currency("CRC", "Costa Rican Colon", "₡", "🇨🇷", country = "Costa Rica", decimalPlaces = 2),
        Currency(
            "DOP",
            "Dominican Peso",
            "RD$",
            "🇩🇴",
            country = "Dominican Republic",
            decimalPlaces = 2
        ),
        Currency("UYU", "Uruguayan Peso", "\$U", "🇺🇾", country = "Uruguay", decimalPlaces = 2),
        Currency("KZT", "Kazakhstani Tenge", "₸", "🇰🇿", country = "Kazakhstan", decimalPlaces = 2),
        Currency("LKR", "Sri Lankan Rupee", "Rs", "🇱🇰", country = "Sri Lanka", decimalPlaces = 2),

        // Crypto Currencies
        Currency("BTC", "Bitcoin", "₿", "🪙", true, "Decentralized", 6),
        Currency("ETH", "Ethereum", "Ξ", "🔷", true, "Decentralized", 6),
        Currency("SOL", "Solana", "◎", "🟣", true, "Solana Network", 4),
        Currency("BNB", "Binance Coin", "BNB", "🟡", true, "BNB Chain", 4),
        Currency("XRP", "XRP / Ripple", "XRP", "✕", true, "Ripple", 4),
        Currency("DOGE", "Dogecoin", "Ð", "🐕", true, "Decentralized", 2),
        Currency("ADA", "Cardano", "₳", "🔵", true, "Cardano", 4),
        Currency("AVAX", "Avalanche", "AVAX", "🔺", true, "Avalanche", 4),
        Currency("LINK", "Chainlink", "LINK", "🔗", true, "Ethereum", 4),
        Currency("DOT", "Polkadot", "DOT", "⚪", true, "Polkadot", 4),
        Currency("MATIC", "Polygon", "POL", "🟣", true, "Polygon", 4),
        Currency("NEAR", "NEAR Protocol", "NEAR", "🟢", true, "NEAR", 4),
        Currency("LTC", "Litecoin", "Ł", "🪙", true, "Decentralized", 4),
        Currency("BCH", "Bitcoin Cash", "BCH", "🟢", true, "Decentralized", 4),
        Currency("SHIB", "Shiba Inu", "SHIB", "🐶", true, "Ethereum", 0),
        Currency("UNI", "Uniswap", "UNI", "🦄", true, "Ethereum", 4),
        Currency("TRX", "TRON", "TRX", "🔴", true, "TRON", 4),
        Currency("XLM", "Stellar Lumens", "XLM", "🚀", true, "Stellar", 4),
        Currency("ATOM", "Cosmos", "ATOM", "⚛️", true, "Cosmos", 4),
        Currency("SUI", "Sui", "SUI", "💧", true, "Sui Network", 4),
        Currency("TON", "Toncoin", "TON", "💎", true, "TON", 4)
    )

    private val currencyMap = allCurrencies.associateBy { it.code }

    fun find(code: String): Currency? = currencyMap[code.uppercase()]

    val defaultSelectedCodes = listOf("USD", "EUR")
}
