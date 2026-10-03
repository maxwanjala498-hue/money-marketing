# Binance Connect (Android)

A native Android application built with Kotlin and Jetpack Compose to securely connect your Binance account, monitor spot balances and real-time market valuations, and share privacy-protected portfolio reports.

## Features

- **Portfolio Overview**: Live calculation of spot net worth in USD and BTC equivalent, 24h PnL return, multi-asset allocation breakdown (BTC, ETH, SOL, BNB, Stables, Alts), and key performance metrics.
- **Spot Balances**: Complete list of assets with symbol, holdings (free and locked in open orders), live USD prices, 24h change %, and portfolio allocation percentages. Includes fast search filter and hide small balances (< $1.00) toggle.
- **Asset Detail & Interactive Charts**: View detailed market statistics (24h high, low, volume) and historical price trend lines rendered dynamically via Canvas using real Binance kline candlestick data (1H/1D intervals). Includes direct shortcut to trade on Binance.
- **Live Market Feed**: Real-time ticker watchlist of top Binance spot trading pairs with price direction indicators and quick trade links.
- **Trade History**: Chronological view of recent filled spot buy/sell executions with quantity, executed price, total quote value, and commission fees.
- **Privacy & Security**: Read-only connection mode with local HMAC SHA-256 request signing. Instant privacy mode masks all dollar figures while keeping percentage returns intact.
- **Instant Demo Mode**: Pre-configured realistic spot account integrated with live Binance market price feeds for testing without API keys.
- **Statement Import**: Parse Binance Spot statements from CSV directly into your portfolio tracker.
- **Portfolio Sharing & Export**: Generate customizable snapshot cards (with trader handle and privacy settings) shareable directly via Android system share intent, formatted text for social media (X, Telegram, Discord), or CSV statements.

## Architecture

- **Language**: Kotlin 2.0
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow
- **Networking**: OkHttp for direct Binance Spot API queries and public tickers
- **Build System**: Gradle with Kotlin DSL
