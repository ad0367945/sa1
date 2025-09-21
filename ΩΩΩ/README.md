# AI Trading Overlay (Full)
- Foreground screen capture (MediaProjection)
- On-device OCR (ML Kit)
- Local RSI/EMA + quick rule -> (BUY/SELL/WAIT)
- Overlay bubble shows live signal
- Optional ChatGPT call (numbers only). To enable, set API key in `AiClient.kt`.

## Build (AIDE/Replit)
1) Open project, grant overlay & screen capture permissions at runtime.
2) Build debug APK.
3) On first run, open Binance price screen; bubble should update.

Note: Heuristic crop rectangles may need calibration per device.
