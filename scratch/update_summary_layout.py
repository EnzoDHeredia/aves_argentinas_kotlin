with open('app/src/main/res/layout/activity_learning_game.xml', 'r', encoding='utf-8') as f:
    c = f.read()

target = """                        <TextView
                            android:id="@+id/learning_summary_congrats"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:gravity="center"
                            android:text="@string/learning_summary_great"
                            android:textColor="@color/text_primary"
                            android:textSize="24sp"
                            android:textStyle="bold" />

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:gravity="center"
                            android:text="@string/learning_summary"
                            android:textColor="@color/text_secondary"
                            android:textSize="15sp" />

                        <com.google.android.material.card.MaterialCardView
                            android:id="@+id/learning_summary_results"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="16dp"
                            app:cardBackgroundColor="@color/brand_surface"
                            app:cardCornerRadius="22dp"
                            app:cardElevation="2dp"
                            app:strokeColor="@color/brand_outline"
                            app:strokeWidth="1dp">

                            <LinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="vertical"
                                android:gravity="center_horizontal"
                                android:padding="@dimen/screen_padding">

                                <com.github.mikephil.charting.charts.PieChart
                                    android:id="@+id/learning_donut_chart"
                                    android:layout_width="190dp"
                                    android:layout_height="190dp"
                                    android:importantForAccessibility="no" />

                                <TextView
                                    android:id="@+id/learning_total"
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="10dp"
                                    android:gravity="center"
                                    android:textColor="@color/text_primary"
                                    android:textSize="17sp"
                                    android:textStyle="bold" />

                                <LinearLayout
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="14dp"
                                    android:orientation="horizontal"
                                    android:baselineAligned="false">

                                    <com.google.android.material.card.MaterialCardView
                                        android:layout_width="0dp"
                                        android:layout_height="wrap_content"
                                        android:layout_weight="1"
                                        android:layout_marginEnd="6dp"
                                        app:cardBackgroundColor="@color/accent_soft"
                                        app:cardCornerRadius="16dp"
                                        app:cardElevation="0dp"
                                        app:strokeColor="@color/brand_primary"
                                        app:strokeWidth="1.5dp">

                                        <TextView
                                            android:id="@+id/learning_known_total"
                                            android:layout_width="match_parent"
                                            android:layout_height="wrap_content"
                                            android:layout_gravity="center"
                                            android:gravity="center"
                                            android:padding="12dp"
                                            android:textColor="@color/brand_primary"
                                            android:textSize="15sp"
                                            android:textStyle="bold" />
                                    </com.google.android.material.card.MaterialCardView>

                                    <com.google.android.material.card.MaterialCardView
                                        android:layout_width="0dp"
                                        android:layout_height="wrap_content"
                                        android:layout_weight="1"
                                        android:layout_marginStart="6dp"
                                        app:cardBackgroundColor="@color/brand_secondary_container"
                                        app:cardCornerRadius="16dp"
                                        app:cardElevation="0dp"
                                        app:strokeColor="@color/brand_secondary"
                                        app:strokeWidth="1.5dp">

                                        <TextView
                                            android:id="@+id/learning_review_total"
                                            android:layout_width="match_parent"
                                            android:layout_height="wrap_content"
                                            android:layout_gravity="center"
                                            android:gravity="center"
                                            android:padding="12dp"
                                            android:textColor="@color/brand_on_secondary_container"
                                            android:textSize="15sp"
                                            android:textStyle="bold" />
                                    </com.google.android.material.card.MaterialCardView>
                                </LinearLayout>

                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="12dp"
                                    android:gravity="center"
                                    android:text="@string/learning_summary_explanation"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="13sp" />
                            </LinearLayout>
                        </com.google.android.material.card.MaterialCardView>

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_review"
                            style="@style/Widget.AvesArgentinas.Button.LearningPrimary"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="18dp"
                            android:minHeight="@dimen/learning_button_height"
                            android:text="@string/learning_review"
                            android:textSize="17sp"
                            android:textStyle="bold" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_again"
                            style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="@dimen/learning_button_height"
                            android:text="@string/learning_again"
                            android:textSize="17sp"
                            android:textStyle="bold" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_choose_mode"
                            style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="@dimen/learning_button_height"
                            android:text="@string/learning_choose_mode"
                            android:textSize="16sp"
                            android:textStyle="bold" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_exit"
                            style="@style/Widget.AvesArgentinas.Button.Tonal"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="@dimen/learning_button_height"
                            android:text="@string/learning_exit"
                            android:textSize="16sp"
                            android:textStyle="bold" />"""

replacement = """                        <TextView
                            android:id="@+id/learning_summary_congrats"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="8dp"
                            android:gravity="center"
                            android:text="@string/learning_summary_great"
                            android:textColor="@color/text_primary"
                            android:textSize="26sp"
                            android:textStyle="bold" />

                        <TextView
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="4dp"
                            android:gravity="center"
                            android:text="@string/learning_summary"
                            android:textColor="@color/text_secondary"
                            android:textSize="14sp" />

                        <com.google.android.material.card.MaterialCardView
                            android:id="@+id/learning_summary_results"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="18dp"
                            app:cardBackgroundColor="@color/brand_surface"
                            app:cardCornerRadius="28dp"
                            app:cardElevation="4dp"
                            app:strokeColor="@color/brand_outline"
                            app:strokeWidth="1dp">

                            <LinearLayout
                                android:layout_width="match_parent"
                                android:layout_height="wrap_content"
                                android:orientation="vertical"
                                android:gravity="center_horizontal"
                                android:paddingStart="20dp"
                                android:paddingEnd="20dp"
                                android:paddingTop="22dp"
                                android:paddingBottom="20dp">

                                <com.github.mikephil.charting.charts.PieChart
                                    android:id="@+id/learning_donut_chart"
                                    android:layout_width="190dp"
                                    android:layout_height="190dp"
                                    android:importantForAccessibility="no" />

                                <TextView
                                    android:id="@+id/learning_total"
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="14dp"
                                    android:gravity="center"
                                    android:textColor="@color/text_primary"
                                    android:textSize="16sp"
                                    android:textStyle="bold" />

                                <LinearLayout
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="14dp"
                                    android:orientation="horizontal"
                                    android:baselineAligned="false">

                                    <com.google.android.material.card.MaterialCardView
                                        android:layout_width="0dp"
                                        android:layout_height="wrap_content"
                                        android:layout_weight="1"
                                        android:layout_marginEnd="6dp"
                                        app:cardBackgroundColor="@color/accent_soft"
                                        app:cardCornerRadius="18dp"
                                        app:cardElevation="0dp"
                                        app:strokeColor="@color/brand_primary"
                                        app:strokeWidth="1.5dp">

                                        <TextView
                                            android:id="@+id/learning_known_total"
                                            android:layout_width="match_parent"
                                            android:layout_height="wrap_content"
                                            android:layout_gravity="center"
                                            android:gravity="center"
                                            android:paddingTop="12dp"
                                            android:paddingBottom="12dp"
                                            android:paddingStart="6dp"
                                            android:paddingEnd="6dp"
                                            android:textColor="@color/brand_primary"
                                            android:textSize="15sp"
                                            android:textStyle="bold" />
                                    </com.google.android.material.card.MaterialCardView>

                                    <com.google.android.material.card.MaterialCardView
                                        android:layout_width="0dp"
                                        android:layout_height="wrap_content"
                                        android:layout_weight="1"
                                        android:layout_marginStart="6dp"
                                        app:cardBackgroundColor="@color/learning_review_container"
                                        app:cardCornerRadius="18dp"
                                        app:cardElevation="0dp"
                                        app:strokeColor="@color/learning_review_btn_bg"
                                        app:strokeWidth="1.5dp">

                                        <TextView
                                            android:id="@+id/learning_review_total"
                                            android:layout_width="match_parent"
                                            android:layout_height="wrap_content"
                                            android:layout_gravity="center"
                                            android:gravity="center"
                                            android:paddingTop="12dp"
                                            android:paddingBottom="12dp"
                                            android:paddingStart="6dp"
                                            android:paddingEnd="6dp"
                                            android:textColor="#C64E18"
                                            android:textSize="15sp"
                                            android:textStyle="bold" />
                                    </com.google.android.material.card.MaterialCardView>
                                </LinearLayout>

                                <TextView
                                    android:layout_width="match_parent"
                                    android:layout_height="wrap_content"
                                    android:layout_marginTop="12dp"
                                    android:gravity="center"
                                    android:text="@string/learning_summary_explanation"
                                    android:textColor="@color/text_secondary"
                                    android:textSize="12sp"
                                    android:lineSpacingMultiplier="1.15" />
                            </LinearLayout>
                        </com.google.android.material.card.MaterialCardView>

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_review"
                            style="@style/Widget.AvesArgentinas.Button.LearningPrimary"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="18dp"
                            android:minHeight="56dp"
                            android:text="@string/learning_review"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            app:icon="@drawable/ic_learning_replay"
                            app:iconGravity="textStart"
                            app:iconPadding="8dp"
                            app:iconSize="20dp"
                            app:cornerRadius="28dp"
                            app:elevation="3dp" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_again"
                            style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="56dp"
                            android:text="@string/learning_again"
                            android:textSize="16sp"
                            android:textStyle="bold"
                            app:icon="@drawable/ic_learning_check"
                            app:iconGravity="textStart"
                            app:iconPadding="8dp"
                            app:iconSize="20dp"
                            app:cornerRadius="28dp"
                            app:elevation="3dp" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_choose_mode"
                            style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="52dp"
                            android:text="@string/learning_choose_mode"
                            android:textSize="15sp"
                            android:textStyle="bold"
                            app:icon="@drawable/ic_learning_quiz"
                            app:iconGravity="textStart"
                            app:iconPadding="8dp"
                            app:iconSize="20dp"
                            app:cornerRadius="28dp" />

                        <com.google.android.material.button.MaterialButton
                            android:id="@+id/learning_exit"
                            style="@style/Widget.AvesArgentinas.Button.LearningOutlined"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginTop="10dp"
                            android:minHeight="52dp"
                            android:text="@string/learning_exit"
                            android:textColor="@color/text_secondary"
                            app:strokeColor="@color/brand_outline"
                            app:strokeWidth="1.5dp"
                            app:backgroundTint="@color/brand_surface"
                            android:textSize="15sp"
                            android:textStyle="bold"
                            app:icon="@drawable/ic_arrow_back"
                            app:iconGravity="textStart"
                            app:iconPadding="8dp"
                            app:iconSize="20dp"
                            app:iconTint="@color/text_secondary"
                            app:cornerRadius="28dp" />"""

assert target in c, "target not found"
c = c.replace(target, replacement)

with open('app/src/main/res/layout/activity_learning_game.xml', 'w', encoding='utf-8', newline='\n') as f:
    f.write(c)

print('Summary layout updated!')
