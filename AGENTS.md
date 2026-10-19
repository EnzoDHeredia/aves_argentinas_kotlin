# Aves Argentinas — instrucciones para agentes

## Proyecto

Este repositorio contiene una aplicación Android para identificar aves y
registrar observaciones. Usa Kotlin, Gradle con Kotlin DSL, Android Views con
layouts XML, componentes Material, Room y TensorFlow Lite.

## Estructura

- `app/src/main/java/com/example/avesargentinas/`: código Kotlin; `ui/` contiene
  pantallas y `data/` la persistencia y los datos de la aplicación.
- `app/src/main/res/`: layouts XML, textos, temas, colores e imágenes.
- `app/src/main/assets/`: modelos de clasificación, etiquetas y metadatos.
- `app/src/test/` y `app/src/androidTest/`: pruebas unitarias e instrumentadas.
- `app/build.gradle.kts`, `gradle/libs.versions.toml` y `gradle/wrapper/`:
  configuración del módulo, catálogo de versiones y Gradle Wrapper.
- `docs/`: documentación; `specs/`: artefactos SDD cuando se use ese flujo.
- `.agents/skills/`: skills y plantillas locales.

## Comandos y validación

Ejecutar desde la raíz del repositorio y usar el Gradle Wrapper. Se necesitan
un JDK compatible con la configuración de Gradle y el Android SDK configurado
en el entorno o en `local.properties`.

| Objetivo | Comando en PowerShell |
| --- | --- |
| Compilar el APK de depuración | `.\gradlew.bat :app:assembleDebug` |
| Ejecutar pruebas unitarias | `.\gradlew.bat :app:testDebugUnitTest` |
| Ejecutar Android Lint | `.\gradlew.bat :app:lintDebug` |
| Ejecutar pruebas instrumentadas | `.\gradlew.bat :app:connectedDebugAndroidTest` |

- Elegir las comprobaciones según el cambio. Las pruebas instrumentadas
  requieren un dispositivo o emulador conectado.
- Para cambios visuales, revisar la pantalla en un dispositivo o emulador
  cuando esté disponible, además de las comprobaciones automatizadas.
- Para cambios exclusivamente documentales, comprobar referencias y formato;
  no hace falta compilar la app.
- Informar qué se verificó, el resultado y cualquier comprobación pendiente.
  No declarar una validación exitosa si no se ejecutó.

## Reglas de trabajo

- Preservar cambios no relacionados y limitar las modificaciones al alcance
  solicitado. Seguir los patrones existentes antes de introducir otros.
- Mantener las interfaces en Android Views y XML, salvo que el usuario apruebe
  una migración. Reutilizar recursos de textos, colores, temas y dimensiones.
- Para diseño visual o cambios de interfaz, seguir
  [frontend-design](.agents/skills/frontend-design/SKILL.md), adaptando sus
  principios visuales a Android y a los componentes existentes. Sus ejemplos
  web no implican introducir HTML, CSS ni frameworks web. No usar `nexus-ui-design`.
- Al cambiar clasificación o assets, mantener la correspondencia entre el
  modelo, el orden de sus etiquetas y los metadatos de especies.
- No versionar rutas locales del SDK, credenciales ni archivos generados.
- Leer cualquier `AGENTS.md` más específico antes de trabajar en su directorio.

## Flujo SDD

Usar desarrollo guiado por especificaciones para features nuevas, cambios de
arquitectura o cuando el usuario lo solicite. Las consultas, revisiones,
cambios documentales y correcciones pequeñas de alcance claro pueden
resolverse directamente, sin crear los tres artefactos. Si el alcance crece
hasta requerir SDD, acordar el nuevo alcance antes de continuar.

1. Usar Superpowers para descubrir el objetivo, explorar alternativas y acordar
   el alcance. Usar `superpowers:brainstorming` antes de redactar la spec.
2. Crear los artefactos con las skills y plantillas locales:
   [spec-generator](.agents/skills/spec-generator/SKILL.md) para el qué y el
   porqué, [plan-generator](.agents/skills/plan-generator/SKILL.md) para el cómo
   y [tasks-generator](.agents/skills/tasks-generator/SKILL.md) para el orden
   de ejecución.
3. Guardarlos juntos en `specs/NNN-<nombre-en-kebab-case>/` como `spec.md`,
   `plan.md` y `tasks.md`, usando el siguiente número libre de tres dígitos.
   Si no existe `specs/`, crearla en la raíz; la ubicación ya está definida.
4. Pedir aprobación explícita de cada artefacto antes de pasar al siguiente
   y aprobar las tareas antes de implementar.
5. Al implementar, ejecutar una sola tarea por turno, correr sus tests,
   actualizar su estado, mostrar el resultado y esperar confirmación antes
   de empezar la siguiente.
6. Aplicar las prácticas de Superpowers de pruebas y verificación sin alterar
   esa cadencia. No hacer commits ni push en ninguna etapa del flujo SDD,
   aunque una skill los indique.

Estas reglas de ubicación, plantillas, aprobaciones y ejecución prevalecen
sobre los valores predeterminados de las skills locales y de Superpowers.
