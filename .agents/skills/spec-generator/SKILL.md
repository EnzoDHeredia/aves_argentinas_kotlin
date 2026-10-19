---
name: spec-generator
description: Crear o revisar una especificación funcional (spec) para una feature antes de planificarla o implementarla. Convierte requisitos en alcance y criterios de aceptación verificables.
---

# Generador de specs

Convierte una idea en una especificación acordada. La spec define el **qué** y el **por qué**; no prescribe cómo implementarlo.

## Antes de redactar

1. Identifica el repositorio al que pertenece la feature. Lee sus instrucciones locales y, si existen, `docs/constitution.md` y las specs anteriores en `specs/`.
2. Si falta información que cambie el alcance, entrevista al usuario con preguntas de una en una, hasta seis como máximo. Prioriza comportamiento, errores, casos límite y exclusiones. No propongas soluciones técnicas.
3. Si no existe `specs/`, pregunta dónde guardar el artefacto antes de crearlo. Si existe, usa `specs/NNN-<nombre-en-kebab-case>/spec.md`, con el siguiente número libre de tres dígitos.

## Redacción

- Elige la plantilla según el tipo de cambio:
  - Feature nueva: [spec-template.md](spec-template.md).
  - Cambio o extensión de una feature existente que debe preservar comportamiento: [spec-existing-feature-template.md](spec-existing-feature-template.md).
  No mezcles ambas; adapta solo las subsecciones necesarias para que el alcance sea claro.
- Escribe los requisitos funcionales como RF-1, RF-2, etc., usando notación EARS. Cada requisito debe describir un comportamiento verificable.
- La spec no incluye stack, arquitectura, nombres de archivos, esquemas de datos, algoritmos ni firmas; eso corresponde al plan.
- Todo dato incierto debe quedar como `[NECESITA ACLARACIÓN: pregunta concreta]`; no lo resuelvas mediante una suposición silenciosa.
- Mantén siempre una sección explícita de "Fuera de alcance".
- Escribe en el idioma de la constitución del proyecto; si no existe, en el idioma del usuario.

## Notación EARS

| Patrón | Forma |
|---|---|
| Ubicuo | `EL SISTEMA <hará>` |
| Evento | `CUANDO <disparador>, EL SISTEMA <hará>` |
| Estado | `MIENTRAS <estado>, EL SISTEMA <hará>` |
| Opcional | `DONDE <característica>, EL SISTEMA <hará>` |
| Error | `SI <condición>, ENTONCES EL SISTEMA <hará>` |

## Cierre

Presenta la spec y pide aprobación explícita. No generes plan, tareas ni código hasta que el usuario la apruebe. Si el usuario pide revisar una spec existente, lista —sin reescribirla— ambigüedades, contradicciones, casos límite faltantes y conflictos con la constitución.
