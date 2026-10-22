with open('app/src/main/res/layout/activity_learning_game.xml', 'r', encoding='utf-8') as f:
    c = f.read()

target1 = """                                <!-- Contenedor e imagen de la foto del ave -->
                                <FrameLayout
                                    android:id="@+id/learning_photo_container"
                                    android:layout_width="match_parent"
                                    android:layout_height="match_parent"
                                    android:importantForAccessibility="noHideDescendants">

                                    <ImageView
                                        android:id="@+id/learning_photo"
                                        android:layout_width="match_parent"
                                        android:layout_height="match_parent"
                                        android:scaleType="centerCrop"
                                        android:contentDescription="@string/learning_photo_description"
                                        android:importantForAccessibility="no"
                                        android:visibility="gone" />
                                </FrameLayout>

                                <!-- Velo inferior para legibilidad del texto blanco -->
                                <View
                                    android:id="@+id/learning_scrim"
                                    android:layout_width="match_parent"
                                    android:layout_height="260dp"
                                    android:layout_gravity="bottom"
                                    android:background="@drawable/bg_learning_scrim"
                                    android:importantForAccessibility="no" />"""

repl1 = """                                <!-- Contenedor e imagen de la foto del ave -->
                                <FrameLayout
                                    android:id="@+id/learning_photo_container"
                                    android:layout_width="match_parent"
                                    android:layout_height="match_parent"
                                    android:background="#121824"
                                    android:importantForAccessibility="noHideDescendants">

                                    <!-- Fondo ambiental suave para rellenar la tarjeta sin recortar el ave -->
                                    <ImageView
                                        android:id="@+id/learning_photo_backdrop"
                                        android:layout_width="match_parent"
                                        android:layout_height="match_parent"
                                        android:scaleType="centerCrop"
                                        android:alpha="0.30"
                                        android:importantForAccessibility="no"
                                        android:visibility="gone" />

                                    <!-- Foto principal: encuadre completo para que el ave se aprecie sin sobre-zoom -->
                                    <ImageView
                                        android:id="@+id/learning_photo"
                                        android:layout_width="match_parent"
                                        android:layout_height="match_parent"
                                        android:scaleType="fitCenter"
                                        android:contentDescription="@string/learning_photo_description"
                                        android:importantForAccessibility="no"
                                        android:visibility="gone" />
                                </FrameLayout>

                                <!-- Velo inferior para legibilidad del texto blanco -->
                                <View
                                    android:id="@+id/learning_scrim"
                                    android:layout_width="match_parent"
                                    android:layout_height="180dp"
                                    android:layout_gravity="bottom"
                                    android:background="@drawable/bg_learning_scrim"
                                    android:importantForAccessibility="no" />"""

target2 = """                                <!-- Botón izquierdo: Quiero repasarla -->
                                <com.google.android.material.button.MaterialButton
                                    android:id="@+id/learning_mark"
                                    style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:layout_marginEnd="6dp"
                                    android:minHeight="56dp"
                                    android:text="@string/learning_mark"
                                    android:textSize="14sp"
                                    android:textStyle="bold"
                                    android:textColor="@color/brand_secondary"
                                    app:backgroundTint="@color/brand_secondary_container"
                                    app:strokeColor="@color/brand_secondary"
                                    app:strokeWidth="1.5dp"
                                    app:icon="@drawable/ic_learning_replay"
                                    app:iconGravity="textStart"
                                    app:iconPadding="6dp"
                                    app:iconSize="20dp"
                                    app:iconTint="@color/brand_secondary"
                                    app:cornerRadius="28dp"
                                    android:contentDescription="@string/learning_mark" />

                                <!-- Botón derecho: La sabía -->
                                <com.google.android.material.button.MaterialButton
                                    android:id="@+id/learning_known"
                                    style="@style/Widget.AvesArgentinas.Button.LearningPrimary"
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:layout_marginStart="6dp"
                                    android:minHeight="56dp"
                                    android:text="@string/learning_known"
                                    android:textSize="14sp"
                                    android:textStyle="bold"
                                    app:strokeColor="@color/brand_primary"
                                    app:strokeWidth="1.5dp"
                                    app:icon="@drawable/ic_learning_check"
                                    app:iconGravity="textStart"
                                    app:iconPadding="6dp"
                                    app:iconSize="20dp"
                                    app:cornerRadius="28dp"
                                    android:contentDescription="@string/learning_known" />"""

repl2 = """                                <!-- Botón izquierdo: Quiero repasar -->
                                <com.google.android.material.button.MaterialButton
                                    android:id="@+id/learning_mark"
                                    style="@style/Widget.AvesArgentinas.Button.Primary"
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:layout_marginEnd="6dp"
                                    android:minHeight="56dp"
                                    android:text="@string/learning_mark"
                                    android:textSize="14sp"
                                    android:textStyle="bold"
                                    android:textColor="@color/learning_review_btn_text"
                                    app:backgroundTint="@color/learning_review_btn_bg"
                                    app:icon="@drawable/ic_learning_replay"
                                    app:iconGravity="textStart"
                                    app:iconPadding="8dp"
                                    app:iconSize="20dp"
                                    app:iconTint="@color/learning_review_btn_text"
                                    app:cornerRadius="28dp"
                                    app:elevation="3dp"
                                    android:contentDescription="@string/learning_mark" />

                                <!-- Botón derecho: La sabía -->
                                <com.google.android.material.button.MaterialButton
                                    android:id="@+id/learning_known"
                                    style="@style/Widget.AvesArgentinas.Button.Primary"
                                    android:layout_width="0dp"
                                    android:layout_height="wrap_content"
                                    android:layout_weight="1"
                                    android:layout_marginStart="6dp"
                                    android:minHeight="56dp"
                                    android:text="@string/learning_known"
                                    android:textSize="14sp"
                                    android:textStyle="bold"
                                    android:textColor="@color/learning_known_btn_text"
                                    app:backgroundTint="@color/learning_known_btn_bg"
                                    app:icon="@drawable/ic_learning_check"
                                    app:iconGravity="textStart"
                                    app:iconPadding="8dp"
                                    app:iconSize="20dp"
                                    app:iconTint="@color/learning_known_btn_text"
                                    app:cornerRadius="28dp"
                                    app:elevation="3dp"
                                    android:contentDescription="@string/learning_known" />"""

assert target1 in c, "target1 not found"
assert target2 in c, "target2 not found"

c = c.replace(target1, repl1).replace(target2, repl2)

with open('app/src/main/res/layout/activity_learning_game.xml', 'w', encoding='utf-8', newline='\n') as f:
    f.write(c)

print('activity_learning_game.xml updated successfully!')
