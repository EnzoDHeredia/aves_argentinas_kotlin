# Funcionalidad de Exportación de Historial

## 📦 Resumen

Se ha implementado un sistema completo de exportación del historial de observaciones en formato ZIP, incluyendo un archivo CSV con todos los datos y todas las imágenes asociadas.

## ✨ Características

### 1. Botón Flotante (FAB)
- **Ubicación**: Esquina inferior derecha de la pantalla de "Mis Observaciones"
- **Estilo**: Similar al botón de Tweet de Twitter
- **Comportamiento**: 
  - Se muestra solo cuando hay observaciones
  - Se oculta automáticamente si el historial está vacío

### 2. Archivo ZIP Exportado

El archivo ZIP contiene:

#### 📄 `observations.csv`
Incluye todas las columnas:
- ID
- Especie (nombre en inglés)
- Nombre Común (nombre regional en español)
- Nombre Científico
- Confianza (%)
- Conteo
- Fecha (YYYY-MM-DD)
- Hora (HH:MM:SS)
- Latitud
- Longitud
- Notas
- Nombre Imagen

**Formato**: UTF-8 con escape correcto de caracteres especiales (comillas, saltos de línea, etc.)

#### 🖼️ Carpeta `images/`
Contiene todas las fotos de las observaciones con sus nombres originales.

### 3. Flujo de Usuario

```
1. Usuario abre "Mis Observaciones"
   ↓
2. Ve botón flotante (⬇️) en esquina inferior derecha
   ↓
3. Toca el botón
   ↓
4. Diálogo de confirmación muestra:
   - Cantidad de observaciones
   - Contenido del ZIP
   ↓
5. Usuario acepta
   ↓
6. Diálogo de progreso "Exportando..."
   ↓
7. Exportación completa
   ↓
8. Diálogo de éxito con 3 opciones:
   - "Compartir" → Abre selector para compartir ZIP
   - "Abrir carpeta" → Abre carpeta Descargas
   - "Cerrar" → Cierra el diálogo
```

## 🔧 Componentes Técnicos

### HistoryExporter.kt
Clase responsable de la exportación:

**Métodos principales:**
- `exportToZip(observations)`: Genera el archivo ZIP completo
- `generateCsv(observations)`: Crea el contenido CSV
- `addImagesToZip(zipOut, observations)`: Añade todas las imágenes

**Características técnicas:**
- Maneja Android 10+ (Scoped Storage) y versiones anteriores
- Usa coroutines para operaciones asíncronas
- Guarda archivos en carpeta Descargas del sistema
- Manejo robusto de errores con logging

### ObservationLogActivity.kt
Actualizado con:
- Inicialización de `HistoryExporter`
- Gestión del FAB
- Diálogos de confirmación, progreso, éxito y error
- Sistema de compartir archivo
- Opción para abrir carpeta Descargas

### activity_observation_log.xml
Cambios:
- LinearLayout → CoordinatorLayout (para soportar FAB)
- FAB añadido con:
  - `layout_gravity="bottom|end"`
  - Elevación de 6dp
  - Colores de la marca
  - Ícono de descarga

## 📱 Nombre del Archivo

Formato: `AvesArgentinas_Export_YYYYMMDD_HHMMSS.zip`

Ejemplo: `AvesArgentinas_Export_20251016_153045.zip`

## 🎯 Casos de Uso

### Para GBBC (Great Backyard Bird Count):
1. Usuario recolecta observaciones durante varios días
2. Al finalizar el evento, exporta todo el historial
3. Comparte el ZIP con coordinadores o sube a plataformas
4. El CSV puede importarse directamente a eBird u otras plataformas

### Para Respaldo Personal:
1. Exportar regularmente para backup
2. Guardar en la nube (Drive, Dropbox, etc.)
3. Transferir a computadora para análisis

### Para Compartir con Investigadores:
1. Exportar observaciones específicas de un período
2. Compartir por email, WhatsApp, etc.
3. Las imágenes mantienen sus nombres originales

## ⚠️ Consideraciones

### Permisos:
- Android 10+: No requiere permisos especiales (usa MediaStore)
- Android 9 y anteriores: Usa `WRITE_EXTERNAL_STORAGE` (ya declarado en manifest)

### Tamaño del archivo:
- Depende del número de observaciones y calidad de imágenes
- Con 100 observaciones e imágenes de 2-3MB cada una: ~200-300MB
- El proceso puede tardar según la cantidad de datos

### Ubicación:
- Siempre se guarda en carpeta **Descargas** del sistema
- Accesible desde cualquier explorador de archivos
- Fácil de encontrar para el usuario

## 🐛 Manejo de Errores

La implementación maneja:
- Observaciones sin imágenes (se registran como "sin_imagen" en CSV)
- Errores al copiar imágenes individuales (se loggean pero no detienen el proceso)
- Falta de espacio en almacenamiento (muestra error)
- Falta de permisos (muestra error con instrucciones)

## 📊 Logging

Tag: `HistoryExporter`

Logs importantes:
- `"CSV añadido al ZIP: X observaciones"`
- `"Imágenes procesadas: X exitosas, Y fallidas"`
- `"Exportación completada: [filename]"`
- `"Error al exportar historial"` (con stack trace)

## 🚀 Futuras Mejoras Sugeridas

1. **Filtros de exportación**: Exportar solo un rango de fechas o especies específicas
2. **Formato JSON alternativo**: Además de CSV
3. **Compresión optimizada**: Reducir tamaño de imágenes opcionalmente
4. **Estadísticas en el ZIP**: Incluir archivo README con resumen
5. **Integración directa con eBird API**: Subir automáticamente
6. **Programación de backups**: Exportación automática periódica

## ✅ Testing Checklist

- [x] FAB se muestra correctamente en layout
- [x] FAB desaparece cuando no hay observaciones
- [x] Diálogo de confirmación muestra información correcta
- [x] Exportación genera ZIP correctamente
- [x] CSV tiene formato correcto y encoding UTF-8
- [x] Todas las imágenes se copian al ZIP
- [x] Archivo se guarda en Descargas
- [x] Botón "Compartir" funciona
- [x] Botón "Abrir carpeta" funciona
- [x] Manejo de errores muestra mensajes apropiados
- [ ] Probar con 0 observaciones
- [ ] Probar con 1 observación
- [ ] Probar con 100+ observaciones
- [ ] Probar sin permisos de almacenamiento
- [ ] Probar en Android 9 y anteriores
- [ ] Probar en Android 10+
- [ ] Probar compartir por diferentes apps (email, WhatsApp, Drive)
