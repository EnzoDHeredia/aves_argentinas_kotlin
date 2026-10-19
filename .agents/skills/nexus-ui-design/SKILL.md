---
name: nexus-ui-design
description: Principios y guías de diseño visual moderno para Nexus Frontend. Combina estética contemporánea de alto impacto con el design system estricto de Nexus (Tailwind 4, tokens semánticos, light/dark mode y componentes existentes). Usar al diseñar, estilizar o refactorizar interfaces de usuario.
---

# Nexus UI Design — Guía de Diseño Visual y Estilos

Esta skill define cómo crear interfaces modernas, atractivas y con acabados de software de alta calidad (estilo Linear, Raycast, Vercel) dentro de Nexus Frontend (`nexus-fe`).

Tanto **Antigravity** como **Codex** deben aplicar estas directivas al diseñar o refactorizar cualquier componente visual.

---

## 1. Reglas Invariantes de Nexus (No Negociables)

1. **Tokens semánticos obligatorios**:
   - Todo color de fondo, texto o borde debe usar los tokens definidos en `src/app/globals.css` y `src/theme/tokens.ts`.
   - **Prohibido**: Utilizar clases con valores HEX o RGB directos (ej. `bg-[#6366f1]`, `text-[#1e1e24]`).
   - Si se requiere un nuevo rol visual, debe agregarse al sistema de tokens y validarse en `src/theme/palette.spec.ts`.

2. **Regla de oro del Modo Claro**:
   - `#f4f2ee` (`bg-background`) está reservado **exclusivamente para el lienzo general** del workspace/pantalla.
   - **Ningún componente, tarjeta, modal o control interno** puede usar `bg-background` o `#f4f2ee` en modo claro.

3. **Reutilizar componentes existentes**:
   - Antes de crear un control, verificar `src/components/ui/` (`Button`, `Input`, `Card`, `Badge`, `Avatar`, `Modal`, `OnboardingStepCard`, etc.).
   - No incorporar librerías externas de UI adicionales (ej. Radix, MUI, Chakra) sin autorización explícita.

4. **Compatibilidad estricta Dark / Light**:
   - Toda interfaz debe funcionar con contraste y legibilidad óptimos tanto en tema oscuro como en tema claro mediante el atributo `data-theme` en `<html>`.

---

## 2. Jerarquía de Profundidad y Superficies (Elevation)

Nexus utiliza una escala de elevación por capas para generar profundidad sin depender de sombras pesadas o artificiales:

| Nivel | Rol semántico | Clase Tailwind | Uso típico |
| :--- | :--- | :--- | :--- |
| **0. Lienzo** | Base general | `bg-background` | Fondo principal de la ventana o workspace. |
| **1. Chrome** | Superficie persistente | `bg-surface` | Sidebar, topbar, barra de navegación. |
| **2. Contenedor** | Superficie de contenido | `bg-card` | Tarjetas principales, modales, wizards. |
| **3. Agrupación** | Superficie elevada | `bg-surface-elevated` | Inputs, selectores, grupos internos de tarjeta. |
| **4. Separación** | Panel secundario | `bg-panel-subtle` o `bg-surface-soft` | Secciones colapsables, callouts, filas hover. |
| **5. Interacción** | Estado temporal | `hover:bg-surface-soft` | Feedback inmediato al pasar el cursor. |

### Bordes y Contornos
- **Bordes estructurales**: Usar `border border-nexus-border` para delimitar tarjetas y contenedores.
- **Bordes interactivos**: Usar `border border-nexus-border-strong` para inputs y campos de formulario.
- **Radios consistentes**:
  - Contenedores principales (tarjetas, modales): `rounded-xl` (`12px`).
  - Elementos interactivos (botones, inputs, badges): `rounded-md` o `rounded-lg` (`6px` - `8px`).

---

## 3. Tipografía y Jerarquía Visual

1. **Intención en el encabezado**:
   - Los títulos principales deben transmitir claridad e impacto (`text-xl` a `text-3xl`, `font-semibold`, `tracking-tight`, `text-foreground`).
   - Usar subtítulos descriptivos que no repitan el título (`text-sm`, `text-text-secondary`).

2. **Dato y contexto**:
   - Números, métricas y balances deben usar cifras tabulares (`tabular-nums font-mono` o `tabular-nums font-semibold`) para evitar desalineaciones en tablas o tarjetas KPI.
   - Las etiquetas de datos secundarios usan `text-xs font-medium text-text-muted` o `uppercase tracking-wider`.

3. **Espaciado y "aire"**:
   - Las tarjetas de formulario o wizards deben usar espaciado respirable: `p-6 sm:p-8`.
   - Separación entre bloques lógicos: `space-y-4` a `space-y-6`.

---

## 4. Microinteracciones y Estados

1. **Transiciones**:
   - Aplicar transiciones cortas y naturales: `transition-all duration-150` o `transition-colors duration-200`.
   - Evitar animaciones lentas (>300ms) en botones o controles interactivos que hagan sentir lenta la aplicación.

2. **Foco visible y Accesible**:
   - Todo elemento interactivo debe tener foco accesible y llamativo:
     `focus-visible:ring-2 focus-visible:ring-primary/60 focus-visible:outline-none`.

3. **Atmósfera y Brillo (Glow)**:
   - Para pantallas de bienvenida o inicio (login/onboarding), utilizar efectos de luz ambiental difusos:
     ```html
     <div aria-hidden="true" className="pointer-events-none fixed inset-0 overflow-hidden">
       <div className="absolute left-1/2 top-[-20%] size-[38rem] -translate-x-1/2 rounded-full bg-primary/5 blur-3xl" />
       <div className="absolute bottom-[-10%] right-[-5%] size-[25rem] rounded-full bg-accent/5 blur-3xl" />
     </div>
     ```
   - Botones primarios pueden incorporar brillo sutil: `shadow-glow-primary`.

4. **Respeto a movimiento reducido**:
   - Para animaciones con `framer-motion` o CSS, considerar `motion-reduce:transition-none` o variantes simplificadas.

---

## 5. Checklist de Verificación de Diseño

Antes de dar por terminado un diseño o refactor en frontend, verificar:
- [ ] ¿Se usaron únicamente tokens semánticos (sin colores HEX sueltos)?
- [ ] ¿Se probó visualmente en tema oscuro Y en tema claro?
- [ ] En modo claro: ¿el fondo de las tarjetas es distinto de `#f4f2ee`?
- [ ] ¿Es responsive (funciona fluidamente tanto en móvil `375px` como en escritorio `1440px`)?
- [ ] ¿Los botones e inputs tienen foco visible operable por teclado?
- [ ] ¿Las acciones principales y secundarias tienen jerarquía clara (`primary-solid` vs `variant="secondary"` / `variant="ghost"`)?
- [ ] ¿Corren con éxito `npm run lint` y `npm run typecheck`?
