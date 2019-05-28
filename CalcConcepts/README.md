# 概要

アルゴリズムについては以下を参考にされたい.基本的にはこの論文の疑似コードを Java で実装している.
https://www.researchgate.net/publication/228618624_V_Parallel_Recursive_Algorithm_for_FCA?enrichId=rgreq-547cbfc6b62c41b76cd44561852da5ed-XXX&enrichSource=Y292ZXJQYWdlOzIyODYxODYyNDtBUzoxMDIwODgxNzIzNzYwNjhAMTQwMTM1MTEyNTg0Ng%3D%3D&el=1_x_3&_esc=publicationCoverPdf

# bit による管理について

本来であれば,オブジェクト i が属性 j を持っているかという情報を表現するのに直感的なデータ構造は以下のような例が考えられる.

boolean[][] context = new boolean[i][j]

boolean 変数のサイズは処理系依存ではあるが,Oracle の JVM では boolean 配列における boolean 値一つのサイズは 8bit とされており,この場合 context の実体は i×j×8bit のメモリを確保する.
オブジェクト,属性の数が増加した際のパフォーマンスを考えて,本プログラムの元となったプログラム(平成 29 年卒業 世木研究室 OB 長尾雅弘氏による)では各オブジェクトが各属性を持っているか否かを bit 列で管理している.
たとえば以下の様な文脈を考える

|          | attr0 | attr1 | attr2 | attr3 |
| :------: | :---: | :---: | :---: | :---: |
| **obj0** |   x   |   0   |   0   |   x   |
| **obj1** |   x   |   x   |   x   |   0   |
| **obj2** |   0   |   0   |   x   |   x   |

このとき bit 列は 100111100011 のようになる

本プログラムにおいてはメモリの確保を int 型で行っており,実際には以下の様な bit 列になる

100100000000000000000000000000001110000000000000000000000000000000110000000000000000000000000000

このように表現することでオブジェクト数や属性数がある程度大きくなったとき,メモリ空間の節約ができる.

なお実際の context は int[objNum * intAttrLen] となっている.ここで, objNum はオブジェクト数で intAttrLen はすべての属性を表現するために必要な int の数を表している.上記の例であれば objNum は 3, intAttrLen は 1 となる(属性数が 4 なので int のサイズを 32bit とすると int 一つ分のメモリで十分である).

# 最後に

このプログラムは長尾氏のプログラムの必要な部分だけを切り取り,その上で修正やコメントの追加,あるいは一部をより Java-like に書き換えたものであり,すべての状況において元のプログラムと同じ出力をする保証はできない.
気になる人はテストコード書いてみて.
