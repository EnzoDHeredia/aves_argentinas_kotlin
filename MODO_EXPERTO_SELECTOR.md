# Selector de Aves en Modo Experto

## Funcionalidad

Cuando el usuario tiene activado el **Modo Experto** y hace clic en "Guardar Observación", aparece un diálogo que permite seleccionar manualmente el ave observada.

## Flujo de Guardado en Modo Experto

### 1. Usuario hace clic en "Guardar"
- Se verifica que haya imagen y predicción disponible
- Se detecta que el Modo Experto está activo

### 2. Diálogo de Selector de Ave
**Layout**: `dialog_bird_selector.xml`

#### Componentes:
- **AutoCompleteTextView**: Campo con autocompletado
  - Muestra todas las aves del `labels_map.json`
  - Permite escribir para filtrar
  - Threshold de 1 carácter para empezar a sugerir
  
- **TextView de Nombre Científico**: 
  - Inicialmente oculto
  - Aparece cuando se selecciona un ave
  - Muestra: "Nombre científico: {nombre}"
  
- **Botones**:
  - "Cancelar": Cierra el diálogo sin guardar
  - "Continuar": Deshabilitado hasta seleccionar un ave

#### Comportamiento:
1. Usuario escribe en el campo
2. Aparecen sugerencias filtradas de nombres comunes
3. Al seleccionar un ave:
   - Se muestra el nombre científico
   - Se habilita el botón "Continuar"
4. Al hacer clic en "Continuar":
   - Se actualiza la predicción con el ave seleccionada
   - Se cierra este diálogo
   - Se abre el diálogo de cantidad

### 3. Diálogo de Cantidad
- Igual que en modo normal
- Usuario selecciona cantidad de individuos

### 4. Guardado
- Se guarda la observación con:
  - Ave seleccionada manualmente
  - Cantidad especificada
  - Imagen capturada
  - Ubicación (si tiene permiso)

## Componentes del Sistema

### BirdLabelsManager
**Archivo**: `BirdLabelsManager.kt`

Clase singleton que gestiona el mapeo de aves desde `labels_map.json`.

#### Funciones principales:
- `loadLabels(context)`: Carga el JSON y crea el mapa
- `getBirdsList(context)`: Retorna lista de `BirdInfo` ordenada
- `getCommonNamesArray(context)`: Array de strings para el adapter
- `getScientificName(context, commonName)`: Busca nombre científico
- `getCommonName(context, scientificName)`: Busca nombre común

#### Data Class:
```kotlin
data class BirdInfo(
    val scientificName: String,  // Ej: "vanellus_chilensis"
    val commonName: String       // Ej: "tero comun"
)
```

### Modificaciones en MainActivity

#### `handleSaveObservationClick()`
- Detecta si está en modo experto
- **Modo experto**: Muestra `showBirdSelectorDialog()` primero
- **Modo normal**: Va directo a `showObservationCountDialog()`

#### `showBirdSelectorDialog(callback)`
- Infla el layout `dialog_bird_selector.xml`
- Configura el `ArrayAdapter` con nombres comunes
- Listeners:
  - `setOnItemClickListener`: Detecta selección del dropdown
  - `addTextChangedListener`: Valida texto escrito manualmente
- Callback: `(scientificName: String, commonName: String) -> Unit`

#### Actualización de Predicción:
```kotlin
lastPrediction = BirdClassifier.Prediction(
    label = scientificName,           // "vanellus_chilensis"
    displayName = commonName,         // "tero comun"
    confidence = currentPrediction.confidence
)
```

## Datos del JSON

**Archivo**: `assets/labels_map.json`

Formato:
```json
{
    "nombre_cientifico_con_guiones": "nombre común",
    "vanellus_chilensis": "tero comun",
    "furnarius_rufus": "hornero",
    ...
}
```

- **Total**: 100 especies de aves argentinas
- **Formato de clave**: lowercase con guiones bajos
- **Formato de valor**: lowercase, sin acentos

## UI/UX

### Estilo Material Design 3
- **Consistencia visual**: Mismo diseño que `dialog_observation_count.xml`
- `MaterialCardView` con fondo `brand_primary_light`
- Border radius 18dp, stroke de 1dp
- `TextInputLayout` con estilo `ExposedDropdownMenu`
- Background del diálogo: `bg_dialog_observation`
- Botones con estilos `Widget.AvesArgentinas.Button.Primary` y `.Outlined`

### Dropdown y UX
- **Dropdown hacia abajo**: `dropDownVerticalOffset="0dp"`
- **Altura limitada**: `dropDownHeight="200dp"` para evitar que ocupe toda la pantalla
- **Nombres capitalizados**: "Tero Comun", "Hornero", etc.
- **Búsqueda desde primer carácter**: `completionThreshold="1"`

### Accesibilidad
- Campo con `completionThreshold="1"` (sugiere desde 1 carácter)
- Texto en color secundario para descripciones
- Botón deshabilitado hasta tener selección válida
- Nombres científicos en itálica y capitalizados: "Vanellus Chilensis"
- Búsqueda case-insensitive

### Validación
- Solo permite continuar si hay un ave seleccionada
- Detecta tanto selección del dropdown como escritura manual
- Coincidencia case-insensitive para búsqueda

## Ventajas del Modo Experto

1. **Corrección Manual**: Si el modelo se equivoca, el usuario puede corregir
2. **Aves No Detectadas**: Permite guardar aves que el modelo no identificó con confianza
3. **Control Total**: El experto decide qué ave registrar
4. **Base de Datos Completa**: Acceso a todas las 100 especies del modelo

## Caso de Uso Típico

1. Usuario fotografía un ave rara
2. Modelo tiene baja confianza (< 55%)
3. Usuario activa Modo Experto en Opciones
4. Vuelve a la pantalla principal
5. Hace clic en "Guardar"
6. Busca y selecciona manualmente el ave correcta
7. Especifica cantidad
8. Guarda la observación con datos precisos

## Testing

### Casos a probar:
- ✅ Selección desde dropdown
- ✅ Escritura manual completa
- ✅ Escritura parcial + dropdown
- ✅ Cancelar en selector de ave
- ✅ Cancelar en selector de cantidad
- ✅ Guardar observación completa
- ✅ Verificar que se guarda con el ave seleccionada
- ✅ Modo normal sigue funcionando igual

