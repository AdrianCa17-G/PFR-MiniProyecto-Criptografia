//> using scala 3.10.1

// Mini Proyecto: Funciones de orden superior en Criptografía
import scala.util.{Try, Success, Failure}

// 1. Funciones de cifrado: (Char, Int) => Char

// Cifrado César: E(x, k) = (pos(x) + k) mod 26
val cifradoCesar: (Char, Int) => Char = (c, k) => {
  require(k >= -25 && k <= 25, s"Clave César inválida: $k (rango -25 a 25)")
  require((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'),
    s"Carácter no soportado por César: '$c'")
  val base = if (c >= 'a') 'a' else 'A'
  // floorMod evita posiciones negativas al descifrar con -k
  (base + math.floorMod(c - base + k, 26)).toChar
}

// Cifrado XOR: E(x, k) = ASCII(x) ^ k
val cifradoXor: (Char, Int) => Char = (c, k) => {
  require(k >= 0 && k <= 127, s"Clave XOR inválida: $k (rango 0 a 127)")
  require(c <= 127, s"Carácter no ASCII: '$c'")
  (c ^ k).toChar
}

// 2. Función de orden superior: recibe la estrategia de cifrado como parámetro
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

// Muestra el resultado usando Success / Failure
def mostrar(caso: String, resultado: Try[String]): Unit = {
  resultado match {
    case Success(texto) => println(s"$caso -> $texto")
    case Failure(ex)    => println(s"$caso -> Error: ${ex.getMessage}")
  }
}

// 3. Caso César
val cesarCifrado = procesarMensaje("FUNCIONAL", 3, cifradoCesar)
val cesarDescifrado = cesarCifrado.flatMap(texto => procesarMensaje(texto, -3, cifradoCesar))
val cesarEjemplo = procesarMensaje("AD", 1, cifradoCesar)

mostrar("César cifrar FUNCIONAL (k=3)", cesarCifrado)
mostrar("César descifrar (k=-3)", cesarDescifrado)
mostrar("César ejemplo AD (k=1)", cesarEjemplo)

// 4. Caso XOR (la misma función y la misma clave cifran y descifran)
val xorCifrado = procesarMensaje("scala", 5, cifradoXor)
val xorDescifrado = xorCifrado.flatMap(texto => procesarMensaje(texto, 5, cifradoXor))

// Tabla de transformación: (carácter, ASCII, resultado ASCII, cifrado)
val xorTabla = "scala".toList.map(c => (c, c.toInt, c ^ 5, (c ^ 5).toChar))

mostrar("XOR cifrar scala (k=5)", xorCifrado)
mostrar("XOR descifrar (k=5)", xorDescifrado)
println(s"Tabla XOR: $xorTabla")

// 5. Manejo de errores: pruebas que fuerzan el fallo
val errorMensajeNulo = procesarMensaje(null, 3, cifradoCesar)
val errorMensajeVacio = procesarMensaje("", 3, cifradoCesar)
val errorClaveGrande = procesarMensaje("FUNCIONAL", 1000000, cifradoCesar)
val errorClaveXorGrande = procesarMensaje("scala", 500, cifradoXor)
val errorClaveXorNegativa = procesarMensaje("scala", -5, cifradoXor)
val errorCaracterEspecial = procesarMensaje("HOLA MUNDO!", 3, cifradoCesar)
val errorEnie = procesarMensaje("AÑO", 3, cifradoCesar)
val errorNoAscii = procesarMensaje("canción", 5, cifradoXor)

mostrar("Mensaje nulo", errorMensajeNulo)
mostrar("Mensaje vacío", errorMensajeVacio)
mostrar("Clave César excesiva", errorClaveGrande)
mostrar("Clave XOR excesiva", errorClaveXorGrande)
mostrar("Clave XOR negativa", errorClaveXorNegativa)
mostrar("Carácter especial", errorCaracterEspecial)
mostrar("Letra Ñ", errorEnie)
mostrar("Carácter no ASCII", errorNoAscii)

// Recuperación elegante de un fallo
val errorRecuperado = errorClaveGrande.getOrElse("No se pudo cifrar")

// 6. Comprobaciones: detienen el script si un resultado no es el esperado
assert(cesarCifrado == Success("IXQFLRQDO"))
assert(cesarDescifrado == Success("FUNCIONAL"))
assert(cesarEjemplo == Success("BE"))
assert(xorCifrado == Success("vfdid"))
assert(xorDescifrado == Success("scala"))
assert(List(errorMensajeNulo, errorMensajeVacio, errorClaveGrande, errorClaveXorGrande,
  errorClaveXorNegativa, errorCaracterEspecial, errorEnie, errorNoAscii).forall(_.isFailure))
println("Todas las comprobaciones pasaron")
