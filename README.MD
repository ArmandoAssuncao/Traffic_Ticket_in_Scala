# Client/Server/ServerName in Scala
---
Desenvolva um sistema distribuído para persistência de dados no modelo chave-valor.
Cada  servidor  de  operações do  sistema  distribuído  deverá oferecer  pelo  menos uma  das
operações a seguir (seguir exatamente essa interface):
- associar(chave, valor)
- remover(chave)
- recuperar(chave)

Todos os servidores de operaçõesdeverão manter uma cópia completa da base de dados,
de modo que cada operação não-idempotente deverá ativarum processo de sincronização para
manter os dados consistentes.

Deverá haver um servidor de nomes  que  receberá como consulta do cliente o nome de
uma operação, e retornará o IP do servidor de operações que implementa a mesma.

O servidor de nomes retornará apenas endereços de nós ativos na rede, e caso mais de
uma máquina ofereça a mesma operação, deverá ser realizado um balanceamento de carga,
alternando os IPs.

Todas as comunicações de rede deverão ser criptografadas.

Codifique um programa cliente para registrar multas associadas a uma placa de veículo,
que possibilite:
- Adicionar uma multa (data, descrição e valor) ao registro de uma placa;
- Listar todas as multas associadas a uma placa;
- Remover todas as multas associadas a uma placa.