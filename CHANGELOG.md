# Changelog

Todas las versiones relevantes de **Tris Brain**. El formato sigue
[Keep a Changelog](https://keepachangelog.com/es/1.1.0/) y el versionado
semántico (`MAYOR.MENOR.PARCHE`).

## [0.1.1] - 2026-10-02

### Añadido
- Icono adaptativo de la app: pulpo kawaii de la suerte con trébol dorado,
  en vectorial (fondo con degradado y versión monocroma para temas).

## [0.1.0] - 2026-10-02

### Añadido
- App Android nativa (Kotlin + Jetpack Compose + Room) para el sorteo **Tris**.
- Descarga on-demand del histórico CSV de la Lotería Nacional y parseo con
  clasificación por turno (Medio, Tres, Extra, Siete, Clásico).
- Análisis por urna independiente (5 posiciones, dígitos 0–9) desde 2018.
- Botón **Actualizar resultados** (upsert incremental en Room).
- Botón **Sugerir combinaciones para hoy**: 5 bloques, uno por sorteo, con
  estrategias de cobertura balanceada, frecuencia suavizada y mixta.
- **Backtest** dígito-por-dígito contra la línea base uniforme en cada tarjeta.
- Guardado automático de pronósticos por fecha y turno.
- Pantalla **Pronósticos guardados** con estado Pendiente/ACERTÓ/No acertó.
- Pantalla **Monitor de sesgo** con ventana rodante (100/300/500/Todo),
  chi-cuadrado por urna, p-valor y corrección de Bonferroni.
- Migraciones de Room (v1 → v4) que preservan el histórico.
- Pruebas unitarias de parser, uniformidad, sugerencias, pronósticos y monitor.

### Notas
- Sorteo justo: no se encontró sesgo explotable. La app prioriza monitoreo y
  cobertura, no predicción.
- La app funciona 100 % on-demand; no sincroniza en segundo plano.
