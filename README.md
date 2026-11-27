# Mini Proyecto: Higher Order Functions en Criptografía

**Asignatura:** Programación Funcional y Reactiva

## Contenido

1. [Objetivos](#1-objetivos)
2. [Requisitos y ejecución](#2-requisitos-y-ejecución)
3. [Funciones de cifrado](#3-funciones-de-cifrado)
4. [Función de orden superior](#4-función-de-orden-superior)
5. [Caso César](#5-caso-césar)
6. [Caso XOR](#6-caso-xor)
7. [Manejo de errores](#7-manejo-de-errores)
8. [Salida completa](#8-salida-completa)
9. [Preguntas para reflexionar](#9-preguntas-para-reflexionar)

## 1. Objetivos

- Aplicar funciones de orden superior.
- Usar funciones como parámetros para definir estrategias de cifrado dinámicas.
- Transformar colecciones inmutables con `map`.
- Manejar excepciones y validar datos.

## 2. Requisitos y ejecución

- Java (probado con la JVM que gestiona scala-cli)
- Scala 3.10.1 y scala-cli, instalados con [Coursier](https://get-coursier.io/) (`cs setup`)

```bash
scala-cli run criptografia.sc
```

En IntelliJ IDEA, [`criptografia.sc`](criptografia.sc) se puede abrir como worksheet: no usa `object` ni `main`, y cada resultado queda en un `val`.

## 3. Funciones de cifrado

Ambas tienen el tipo `(Char, Int) => Char`. Cada una valida su clave y sus caracteres con `require`, que lanza `IllegalArgumentException`.

### Cifrado César: `E(x, k) = (pos(x) + k) mod 26`

```scala
val cifradoCesar: (Char, Int) => Char = (c, k) => {
  require(k >= -25 && k <= 25, s"Clave César inválida: $k (rango -25 a 25)")
  require((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'),
    s"Carácter no soportado por César: '$c'")
  val base = if (c >= 'a') 'a' else 'A'
  // floorMod evita posiciones negativas al descifrar con -k
  (base + math.floorMod(c - base + k, 26)).toChar
}
```

### Cifrado XOR: `E(x, k) = ASCII(x) ^ k`

```scala
val cifradoXor: (Char, Int) => Char = (c, k) => {
  require(k >= 0 && k <= 127, s"Clave XOR inválida: $k (rango 0 a 127)")
  require(c <= 127, s"Carácter no ASCII: '$c'")
  (c ^ k).toChar
}
```

| Método | Claves válidas | Caracteres soportados |
|--------|----------------|-----------------------|
| César | -25 a 25 | Letras A-Z y a-z (sin Ñ, tildes ni espacios) |
| XOR | 0 a 127 | ASCII (0 a 127) |

Con XOR, el texto cifrado puede contener caracteres de control no imprimibles, según la clave.

## 4. Función de orden superior

`procesarMensaje` recibe el mensaje, la clave y la función de cifrado. Aplica la función a cada carácter con `map` y devuelve un `Try[String]`.

```scala
def procesarMensaje(mensaje: String, clave: Int,
                    cifrado: (Char, Int) => Char): Try[String] = {
  if (mensaje == null) {
    Failure(new IllegalArgumentException("El mensaje es nulo"))
  } else if (mensaje.isEmpty) {
    Failure(new IllegalArgumentException("El mensaje está vacío"))
  } else {
    Try(mensaje.map(c => cifrado(c, clave)))
  }
}
```

El resultado se interpreta con `Success` / `Failure`:

```scala
def mostrar(caso: String, resultado: Try[String]): Unit = {
  resultado match {
    case Success(texto) => println(s"$caso -> $texto")
    case Failure(ex)    => println(s"$caso -> Error: ${ex.getMessage}")
  }
}
```

## 5. Caso César

```scala
val cesarCifrado = procesarMensaje("FUNCIONAL", 3, cifradoCesar)
val cesarDescifrado = cesarCifrado.flatMap(texto => procesarMensaje(texto, -3, cifradoCesar))
val cesarEjemplo = procesarMensaje("AD", 1, cifradoCesar)
```

| Variable | Resultado |
|----------|-----------|
| `cesarCifrado` | `Success(IXQFLRQDO)` |
| `cesarDescifrado` | `Success(FUNCIONAL)` |
| `cesarEjemplo` | `Success(BE)` |

## 6. Caso XOR

El operador `^` está dentro de la lambda `cifradoXor`. La misma función y la misma clave cifran y descifran.

```scala
val xorCifrado = procesarMensaje("scala", 5, cifradoXor)
val xorDescifrado = xorCifrado.flatMap(texto => procesarMensaje(texto, 5, cifradoXor))
val xorTabla = "scala".toList.map(c => (c, c.toInt, c ^ 5, (c ^ 5).toChar))
```

| Variable | Resultado |
|----------|-----------|
| `xorCifrado` | `Success(vfdid)` |
| `xorDescifrado` | `Success(scala)` |

Tabla de transformación (`xorTabla`):

| Carácter | ASCII | XOR 5 | Resultado ASCII | Cifrado |
|----------|-------|-------|-----------------|---------|
| s | 115 | 115 ^ 5 | 118 | v |
| c | 99 | 99 ^ 5 | 102 | f |
| a | 97 | 97 ^ 5 | 100 | d |
| l | 108 | 108 ^ 5 | 105 | i |
| a | 97 | 97 ^ 5 | 100 | d |

## 7. Manejo de errores

Cada prueba fuerza un fallo. El programa no se detiene: el error queda capturado en un `Failure`.

```scala
val errorMensajeNulo = procesarMensaje(null, 3, cifradoCesar)
val errorMensajeVacio = procesarMensaje("", 3, cifradoCesar)
val errorClaveGrande = procesarMensaje("FUNCIONAL", 1000000, cifradoCesar)
val errorClaveXorGrande = procesarMensaje("scala", 500, cifradoXor)
val errorClaveXorNegativa = procesarMensaje("scala", -5, cifradoXor)
val errorCaracterEspecial = procesarMensaje("HOLA MUNDO!", 3, cifradoCesar)
val errorEnie = procesarMensaje("AÑO", 3, cifradoCesar)
val errorNoAscii = procesarMensaje("canción", 5, cifradoXor)

// Recuperación elegante de un fallo
val errorRecuperado = errorClaveGrande.getOrElse("No se pudo cifrar")
```

| Variable | Mensaje de error capturado |
|----------|----------------------------|
| `errorMensajeNulo` | El mensaje es nulo |
| `errorMensajeVacio` | El mensaje está vacío |
| `errorClaveGrande` | Clave César inválida: 1000000 (rango -25 a 25) |
| `errorClaveXorGrande` | Clave XOR inválida: 500 (rango 0 a 127) |
| `errorClaveXorNegativa` | Clave XOR inválida: -5 (rango 0 a 127) |
| `errorCaracterEspecial` | Carácter no soportado por César: ' ' |
| `errorEnie` | Carácter no soportado por César: 'Ñ' |
| `errorNoAscii` | Carácter no ASCII: 'ó' |

`errorRecuperado` vale `"No se pudo cifrar"`.

Al final, el script verifica los resultados esperados con `assert`.

## 8. Salida completa

```text
César cifrar FUNCIONAL (k=3) -> IXQFLRQDO
César descifrar (k=-3) -> FUNCIONAL
César ejemplo AD (k=1) -> BE
XOR cifrar scala (k=5) -> vfdid
XOR descifrar (k=5) -> scala
Tabla XOR: List((s,115,118,v), (c,99,102,f), (a,97,100,d), (l,108,105,i), (a,97,100,d))
Mensaje nulo -> Error: El mensaje es nulo
Mensaje vacío -> Error: El mensaje está vacío
Clave César excesiva -> Error: requirement failed: Clave César inválida: 1000000 (rango -25 a 25)
Clave XOR excesiva -> Error: requirement failed: Clave XOR inválida: 500 (rango 0 a 127)
Clave XOR negativa -> Error: requirement failed: Clave XOR inválida: -5 (rango 0 a 127)
Carácter especial -> Error: requirement failed: Carácter no soportado por César: ' '
Letra Ñ -> Error: requirement failed: Carácter no soportado por César: 'Ñ'
Carácter no ASCII -> Error: requirement failed: Carácter no ASCII: 'ó'
Todas las comprobaciones pasaron
```

## 9. Preguntas para reflexionar

### ¿Por qué XOR es su propia inversa y César requiere invertir el signo de la clave?

XOR compara bit a bit y cumple `k ^ k = 0` y `x ^ 0 = x`. Por eso `(x ^ k) ^ k = x ^ (k ^ k) = x`: aplicar la misma operación dos veces devuelve el original. César es una suma módulo 26; sumar `k` otra vez desplaza más (`x + 2k`). Para volver se debe sumar el inverso aditivo, `-k` (o `26 - k`).

### ¿Qué ventajas ofrece `map` sobre un bucle `for` tradicional?

- Es declarativo: se indica qué transformación aplicar, no cómo recorrer.
- No necesita índices ni variables mutables, así que evita errores de límites.
- Devuelve una colección nueva y deja intacto el mensaje original.
- Recibe la función como argumento, por lo que cambiar de cifrado no cambia el recorrido.

(En Scala, `for ... yield` se traduce a `map`; la comparación es con el bucle imperativo que acumula en una variable.)

### ¿Cómo ayuda el tipado estático al pasar funciones como argumentos?

`procesarMensaje` exige `(Char, Int) => Char`. Si se pasa una función con otros parámetros u otro tipo de retorno, el código no compila, y el error aparece antes de ejecutar. El tipo no controla los valores (por ejemplo, una clave fuera de rango); para eso están `require` y `Try`.
