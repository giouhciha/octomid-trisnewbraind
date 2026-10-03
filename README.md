# Tris Brain

App nativa de Android para analizar el histórico del sorteo **Tris** de la Lotería
Nacional y **sugerir combinaciones del día, sorteo por sorteo**.

> Importante: Tris usa 5 urnas independientes (0–9). La probabilidad base es
> `1/10` por dígito y `1/100,000` por combinación exacta, sin importar la
> combinación elegida. La app no promete predecir el futuro: mide desviaciones
> reales y juega con cobertura. Si algún método no supera al azar, la propia UI lo dice.

## Origen de datos

`https://www.loterianacional.gob.mx/Home/Historicos?ARHP=VAByAGkAcwA=`

El endpoint responde **CSV** con el formato:

```
NPRODUCTO,CONCURSO,R1,R2,R3,R4,R5,FECHA,Multiplicador
```

- Antes del 03/09/2007 el sorteo era de **4 dígitos** (`R5` vacío): se descarta.
- `turno` se infiere por orden **ascendente** de `CONCURSO` dentro del mismo día:
  `1 = Medio día (13h)`, `2 = Tres (15h)`, `3 = Extra (17h)`, `4 = Siete (19h)`,
  `5 = Clásico (21h)`. En 2018+ todos los días tienen exactamente 5 concursos.
- `Multiplicador = SI/NO` es un flag aleatorio por concurso; se guarda como dato informativo.

El análisis por defecto usa **2018-01-01 en adelante** (15,420 concursos).

## Uso

1. **Actualizar resultados** — descarga el CSV completo y hace *upsert* de los
   concursos nuevos en Room (incluye el día anterior).
2. **Sugerir combinaciones para hoy** — analiza cada turno por separado (solo sus
   concursos) y genera N combinaciones por sorteo, sin repetirlas entre turnos.
   Cada generación se **guarda automáticamente por fecha y turno**.
3. **Ver pronósticos guardados** — historial de pronósticos por fecha, sorteo,
   estrategia y combinaciones. Cuando llegan los resultados, marca si alguna
   combinación acertó.
4. **Monitor de sesgo** — analiza los últimos 100/300/500 sorteos o todo el
   histórico por turno y posición, con corrección de Bonferroni para no confundir
   ruido con señal. Solo marca una urna como sesgada si su desviación es real y
   sostenida.

### Estrategias

- **Cobertura balanceada** (default): reparte cada dígito de forma pareja en cada
  urna. Es la única defendible si el sorteo es justo.
- **Frecuencia suavizada**: dígitos más vistos por posición (Laplace +0.5). Sin evidencia de ventaja.
- **Mixta**: mitad cobertura, mitad frecuencia.

Cada tarjeta muestra un **backtest** (aciertos por dígito y log-loss) contra el azar.

## Estado estadístico (verificado sobre 25,690 sorteos de 5 dígitos)

- Chi-cuadrado por posición (2018+): `9.1, 7.2, 9.9, 7.9, 5.2` — todos por debajo
  del umbral 5% (16.92).
- Sin dependencia lag-1 ni entre urnas relevante.
- Backtest walk-forward de frecuencia vs. uniforme: ninguna estrategia supera
  `log-loss = 2.30259` ni `10.0%` de aciertos. Las ventanas cortas lo empeoran.

Conclusión: hoy no hay señal explotable. La app prioriza **monitorear sesgo** y
**cubrir el espacio**, no vender predicciones.

## Arquitectura

```
app/src/main/java/com/octomid/trisbraind/
├─ di/            AppContainer (DI manual, sin Hilt)
├─ data/
│  ├─ remote/     TrisCsvDataSource, TrisCsvParser
│  ├─ local/      DrawEntity, DrawDao, PredictionEntity, PredictionDao, TrisDatabase (Room)
│  └─ repo/       TrisRepository, PredictionRepository
├─ domain/
│  ├─ Turno.kt
│  ├─ stats/      FrequencyAnalyzer, UniformityTest (chi² + p-valor), Backtest, BiasMonitor
│  └─ suggest/    SuggestionEngine, Strategy
└─ ui/            MainViewModel, DashboardScreen, HistoryScreen, BiasScreen, theme
```

- **Kotlin + Jetpack Compose + Room**, `minSdk 26`, `compileSdk 35`.
- Sin backend, sin GPU, sin ML: todo es O(n) en el teléfono.

## Compilar

Requiere el JDK de Android Studio (JBR).

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Pruebas unitarias:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

También se puede abrir la carpeta directamente en Android Studio.
