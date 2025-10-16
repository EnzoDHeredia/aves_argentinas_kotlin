# Nuevas Funcionalidades - Aves Argentinas

## 📋 Resumen de Cambios

Se ha implementado un sistema completo de menú principal y opciones para preparar la app para el Great Backyard Bird Count (GBBC).

## 🆕 Nuevas Pantallas

### 1. **MainMenuActivity** - Menú Principal
- Primera pantalla que ve el usuario al abrir la app
- Opciones disponibles:
  - 📷 **Capturar/Identificar**: Abre la pantalla de identificación
  - 📋 **Mis Observaciones**: Ver historial de observaciones
  - ⚙️ **Opciones**: Configurar la aplicación

### 2. **SettingsActivity** - Pantalla de Opciones
Permite configurar:

#### **Usuario**
- **Nombre de usuario**: Campo de texto para identificar al observador

#### **Identificación**
- **Modo experto**: Permite guardar observaciones sin importar el porcentaje de confianza del modelo
  - En modo normal: Solo se puede guardar si confianza ≥ 55%
  - En modo experto: Se puede guardar cualquier predicción
  - Las predicciones con baja confianza muestran ⚠️ (Modo experto)

- **Captura instantánea**: Al abrir la app, salta directamente a la pantalla de captura/identificación (MainActivity) sin pasar por el menú
  - Útil para flujos de trabajo rápidos
  - La app funciona como si MainActivity fuera la pantalla principal

#### **Privacidad**
- **Ofuscar ubicación exacta**: Añade un desplazamiento aleatorio de hasta 500 metros a las coordenadas GPS
  - Protege tu ubicación exacta en observaciones compartidas
  - El desplazamiento es diferente para cada observación

#### **Apariencia**
- **Modo oscuro**: Alterna entre tema claro y oscuro
  - Se mantiene la funcionalidad existente del botón en la pantalla de identificación

## 🔧 Componentes Técnicos

### SettingsManager
Objeto singleton que gestiona todas las preferencias:
- `getUserName()` / `setUserName()`
- `isExpertMode()` / `setExpertMode()`
- `isObfuscateLocation()` / `setObfuscateLocation()`
- `isInstantCapture()` / `setInstantCapture()`

Almacena datos en SharedPreferences (`app_settings`).

### LocationProvider - Ofuscación de Coordenadas
- Nueva función `obfuscateCoordinates()` que desplaza coords aleatoriamente
- Se aplica automáticamente si `isObfuscateLocation()` es true
- Algoritmo:
  - Desplazamiento máximo: 500 metros
  - Dirección: Aleatoria (norte, sur, este, oeste)
  - Conversión precisa usando coseno de latitud

### MainActivity - Integraciones
1. **Modo Experto**:
   - Modifica `classifyBitmap()` para permitir guardar con cualquier confianza
   - Actualiza `buildResultText()` para mostrar advertencia visual

2. **Captura Instantánea**:
   - En `MainMenuActivity.onCreate()`, verifica si está habilitado
   - Si es true, lanza MainActivity inmediatamente y cierra el menú con `finish()`
   - Resultado: La app se abre directamente en la pantalla de identificación

3. **Navegación mejorada**:
   - Override de `onBackPressed()` en MainActivity
   - Al presionar atrás, vuelve al MainMenuActivity en lugar de cerrar la app
   - Usa flags `CLEAR_TOP` y `SINGLE_TOP` para reutilizar instancia del menú

## 📱 Flujo de Usuario

```
Modo Normal:
[App Launch] → [MainMenuActivity] → (usuario elige) → [MainActivity/Observaciones/Opciones]

Modo Captura Instantánea:
[App Launch] → [MainMenuActivity] → (auto-redirect) → [MainActivity] ✨
                 ↓
            (finish inmediato)
```

## 🎯 Utilidad para GBBC

### Preparación para Great Backyard Bird Count:
1. **Nombre de usuario**: Identifica al participante en las observaciones
2. **Modo experto**: Permite registrar avistamientos dudosos para revisión posterior
3. **Ofuscación de ubicación**: Cumple con requisitos de privacidad al compartir datos
4. **Captura instantánea**: Acelera el proceso de registro durante sesiones de conteo

## 🚀 Próximos Pasos Sugeridos

Para maximizar la utilidad en GBBC:

1. **Export CSV/JSON**: Implementar función para exportar todas las observaciones
2. **Modo Sesión**: Agrupar observaciones de una salida de campo
3. **Integración eBird API**: Subir observaciones directamente a eBird
4. **Mapa de observaciones**: Visualizar puntos en un mapa
5. **Estadísticas**: Dashboard con conteos por especie/fecha

## 📄 Archivos Modificados/Creados

### Nuevos:
- `SettingsManager.kt` - Gestión de preferencias
- `MainMenuActivity.kt` - Menú principal
- `SettingsActivity.kt` - Pantalla de opciones
- `activity_main_menu.xml` - Layout del menú
- `activity_settings.xml` - Layout de opciones

### Modificados:
- `MainActivity.kt` - Integración modo experto y captura instantánea
- `LocationProvider.kt` - Ofuscación de coordenadas
- `AndroidManifest.xml` - Registro de nuevas activities y cambio de LAUNCHER

## ✅ Testing Checklist

- [ ] Abrir app → debe mostrar MainMenuActivity
- [ ] Botón "Capturar" → abre MainActivity
- [ ] Botón "Mis Observaciones" → abre ObservationLogActivity
- [ ] Botón "Opciones" → abre SettingsActivity
- [ ] Cambiar nombre de usuario → se guarda y persiste
- [ ] Activar modo experto → permite guardar con baja confianza
- [ ] Activar captura instantánea → al abrir la app se salta el menú y va directo a MainActivity
- [ ] Activar ofuscación → coordenadas guardadas son diferentes a las reales
- [ ] Cambiar tema → alterna entre claro/oscuro correctamente
- [ ] Todas las configuraciones persisten al cerrar y reabrir la app
