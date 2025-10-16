# 🧪 Pruebas - Campo de Notas

## Cambios Realizados

### 1. **Layout - RelativeLayout en lugar de LinearLayout**
- ✅ Cambio a `RelativeLayout` como contenedor raíz
- ✅ Agregado `android:clickable="true"` y `android:focusable="true"` al root
- ✅ El toolbar está posicionado con `layout_alignParentTop="true"`
- ✅ El ScrollView está con `layout_below="@id/toolbarCard"`
- ✅ Mejor manejo del teclado con esta estructura

### 2. **AndroidManifest - windowSoftInputMode**
```xml
android:windowSoftInputMode="adjustResize|stateHidden"
```
- `adjustResize`: La pantalla se ajusta cuando aparece el teclado
- `stateHidden`: El teclado está oculto al abrir la actividad

### 3. **Código Kotlin - Mejoras**

#### Touch Listener Mejorado:
- Listener en `rootLayout` Y en `scrollView`
- Usa `getLocationOnScreen()` para coordenadas precisas
- Compara con `rawX` y `rawY` del evento
- Método separado `handleTouchOutside()` para claridad

#### Scroll Automático:
```kotlin
edtNotes.setOnFocusChangeListener { view, hasFocus ->
    if (hasFocus) {
        scrollView.postDelayed({
            scrollView.smoothScrollTo(0, view.bottom)
        }, 300)
    }
}
```
- Cuando el campo obtiene foco, hace scroll automáticamente
- Delay de 300ms para que el teclado termine de aparecer

## 🎯 Qué Probar

### Test 1: Apertura del Teclado
1. Abre una observación guardada
2. Toca el campo de notas
3. **Resultado esperado**: 
   - ✅ El teclado aparece
   - ✅ El campo completo es visible (no tapado)
   - ✅ El scroll se ajusta automáticamente

### Test 2: Tocar Fuera del Campo
1. Con el teclado abierto y el cursor en notas
2. Toca cualquier parte de la pantalla (imagen, texto, espacio vacío)
3. **Resultado esperado**:
   - ✅ El teclado se cierra
   - ✅ Aparece "✓ Guardado" en verde
   - ✅ El campo pierde el foco

### Test 3: Botón "Done" del Teclado
1. Con el teclado abierto
2. Presiona el botón "✓" (Done) del teclado
3. **Resultado esperado**:
   - ✅ El teclado se cierra
   - ✅ Aparece "✓ Guardado" en verde
   - ✅ El campo pierde el foco

### Test 4: Salir de la Pantalla
1. Escribe algo en notas
2. Presiona el botón atrás
3. Vuelve a entrar al detalle
4. **Resultado esperado**:
   - ✅ Las notas se guardaron automáticamente

### Test 5: Scroll con Teclado Abierto
1. Abre el teclado
2. Intenta hacer scroll hacia arriba para ver la imagen
3. **Resultado esperado**:
   - ✅ Puedes hacer scroll libremente
   - ✅ El teclado permanece abierto si el campo tiene foco

## 🐛 Si Algo No Funciona

### Problema: El teclado sigue tapando el campo
**Solución**: Verifica que el `windowSoftInputMode` esté en el Manifest

### Problema: Tocar fuera no cierra el teclado
**Posible causa**: El evento de toque es interceptado por otro view
**Debug**: Agrega logs en `handleTouchOutside()` para ver si se llama

### Problema: No aparece el mensaje "✓ Guardado"
**Verificar**: 
- Que `tilNotes` no sea null
- Que se esté ejecutando en el hilo principal (runOnUiThread)

## 📋 Checklist de Validación

- [ ] El teclado NO tapa el campo de notas
- [ ] Tocar CUALQUIER parte fuera del campo cierra el teclado
- [ ] El mensaje "✓ Guardado" aparece en verde
- [ ] El scroll funciona correctamente con el teclado abierto
- [ ] Los botones "Compartir" y "Eliminar" están al fondo del scroll
- [ ] Las notas se guardan automáticamente al salir
- [ ] El botón "Done" cierra el teclado y guarda

