---
name: tasks-generator
description: Descomponer una spec y un plan técnico aprobados en tareas de implementación pequeñas, ordenadas y verificables. No usar para diseñar la solución ni para implementar código.
---

# Generador de tareas

Convierte un plan técnico aprobado en una lista de ejecución concreta. Las tareas expresan el **orden de trabajo**; no redefinen el producto ni el diseño.

## Antes de descomponer

1. Identifica el repositorio objetivo y lee sus instrucciones locales, la spec aprobada y el plan aprobado.
2. Si cualquiera de los dos artefactos no tiene aprobación explícita, detente y solicita la aprobación faltante.
3. Comprueba que el plan cubra todos los RF. Si hay huecos, contradicciones o decisiones pendientes que impidan estimar tareas, devuélvelo al plan en vez de inventar trabajo.

## Redacción

- Elige la plantilla según el tamaño del trabajo:
  - Feature con varias áreas, dependencias o migraciones: [tasks-template.md](tasks-template.md).
  - Cambio acotado en una sola área: [tasks-compact-template.md](tasks-compact-template.md), con RF y condición de terminación en cada línea.
  Guárdalo junto a la spec como `tasks.md`, salvo que el repositorio establezca una convención distinta.
- Una tarea debe poder completarse y verificarse de manera independiente. Divide las tareas demasiado amplias; no fragmentes tareas triviales solo por alcanzar un número.
- Ordénalas según dependencias reales. Indica dependencias solo cuando no sean obvias.
- En la plantilla completa, cada tarea debe nombrar los RF que habilita, los archivos o áreas afectadas y una comprobación observable. En la compacta, debe nombrar como mínimo los RF y la condición de terminación.
- Incluye tareas de migración, documentación, pruebas o despliegue únicamente cuando el plan las justifique.
- No agregues requisitos nuevos, cambios de alcance ni soluciones alternativas.

## Cierre

Presenta las tareas para confirmación. Tras la confirmación, implementa únicamente las tareas aprobadas y actualiza su estado al completarlas. Si el usuario pide revisar tareas existentes, señala dependencias erróneas, cobertura incompleta, tamaño excesivo y verificaciones ausentes sin reescribirlas salvo que lo pida.
