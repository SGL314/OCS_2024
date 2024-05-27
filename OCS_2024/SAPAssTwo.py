keys = {
'ADD B':'80',
'MOV B,A':'47',
'ADD C':'81',
'MOV B.C':'41',
'ΑΝΑ Β':'A0',
'MOV C,A':'4F',
'ANA C':'Al',
'MOV C.B':'48',
'ANI byte':'E6',
'MVI A,byte':'3E',
'CALL endereco':'CD',
'MVI B,byte':'06',
'CMA':'2F',
'MVI C,byte':'OE',
'DCR A':'3D',
'NOP':'00',
'DCR B':'05',
'ORA B':'BO',
'DCR C':'OD',
'ORA C':'BI',
'HLT':'76',
'ORI byte':'F6',
'IN byte':'DB',
'OUT byte':'D3',
'INR A':'3C',
'RAL':'17',
'INR B':'04',
'RAR':'IF',
'INR C':'OC',
'RET':'C9',
'JM endereco':'FA',
'STA endereco':'32',
'JMP endereco':'C3',
'SUB B':'90',
'JNZ endereco':'C2',
'SUB C':'91',
'JZ endereco':'CA',
'XRA B':'A8',
'LDA endereco':'3A',
'XRA C':'A9',
'MOV A,B':'78',
'XRI byte':'EE',
'MOV A,C':'79'
}
chaves = []
valores = []

for ast in keys.items():
    chaves.append(ast[0])
    valores.append(ast[1])

# print("{",end="")    
# for key in valores:
#     print(f"\"{key}\"",end=",")
# print("}")
for key in chaves:
    print(key)