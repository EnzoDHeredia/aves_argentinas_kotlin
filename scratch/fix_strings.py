with open('app/src/main/res/values/strings.xml', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('<string name="learning_known">La sabía →</string>', '<string name="learning_known">La sabía</string>')
content = content.replace('<string name="learning_mark">← Quiero repasarla</string>', '<string name="learning_mark">Quiero repasar</string>')
content = content.replace('<string name="learning_self_assessment">← Repasar · Deslizá o tocá · La sabía →</string>', '<string name="learning_self_assessment">Repasar · Deslizá o tocá · La sabía</string>')

with open('app/src/main/res/values/strings.xml', 'w', encoding='utf-8', newline='\n') as f:
    f.write(content)

print('strings.xml updated!')
