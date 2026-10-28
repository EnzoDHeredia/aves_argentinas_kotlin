with open('app/build/t4/categorizacion.txt', encoding='utf-8', errors='ignore') as f:
    text = f.read()

start = text.find('PAGE 23')
end = text.find('PAGE 27')
print(text[start:end])
