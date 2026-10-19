---
name: plan-generator
description: Crear o revisar un plan técnico para una feature a partir de una spec aprobada. Define el enfoque de implementación y validación sin escribir código ni descomponerlo aún en tareas.
---

# Generador de planes

Transforma una spec aprobada en un plan técnico ejecutable. El plan define el **cómo** respetando por completo el alcance acordado.

## Antes de planificar

1. Identifica el repositorio objetivo y lee sus instrucciones locales, convenciones y la spec correspondiente.
2. Comprueba que la spec esté aprobada por el usuario. Si no hay evidencia de aprobación, detente y solicítala; no suplantes esa decisión.
3. Inspecciona solo el código y la documentación necesarios para entender los puntos de integración reales. No inventes componentes, dependencias o patrones que el proyecto no necesita.
4. Si un requisito es ambiguo o entra en conflicto con el contexto técnico, señala la duda y pide una decisión antes de fijar el diseño.

## Redacción

- Elige la plantilla según el contexto:
  - Proyecto o feature general: [plan-template.md](plan-template.md).
  - Herramienta CLI o módulo local nuevo: [plan-cli-template.md](plan-cli-template.md), basada en módulos, contratos y estrategia de tests.
  No fuerces la plantilla de CLI para APIs, interfaces web o integraciones.
  Guárdalo junto a la spec como `plan.md`, salvo que las convenciones del repositorio indiquen otro lugar.
- Vincula cada decisión relevante con los RF que satisface, sin alterar ni ampliar la spec.
- Incluye cambios de datos, contratos, permisos, migraciones, observabilidad y compatibilidad solo cuando apliquen.
- Describe archivos, módulos o áreas reales a modificar, creando nuevas piezas únicamente cuando estén justificadas.
- Define una estrategia de validación trazable a los requisitos y casos límite.
- Marca cualquier incertidumbre como `[NECESITA ACLARACIÓN: pregunta concreta]`.

## Cierre

Presenta el plan y pide aprobación explícita. No generes tareas ni escribas código hasta que el usuario apruebe el plan. Al revisar un plan existente, reporta huecos de trazabilidad, riesgos técnicos, cambios de alcance y validaciones ausentes; no lo reescribas sin indicación.
