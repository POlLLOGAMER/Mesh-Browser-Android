package com.example.data.repository

data class LocalWebPage(
    val url: String,
    val title: String,
    val textContent: String,
    val category: String,
    val sizeBytesCompressed: Long,
    val sizeBytesOriginal: Long,
    val hasImages: Boolean = false
)

object WebCatalog {
    val PAGES = listOf(
        LocalWebPage(
            url = "wikipedia.org/wiki/Mesh_Network",
            title = "Red de Malla (Mesh Network) - Wikipedia",
            textContent = """
                Una red de malla (Mesh Network) es una topología de red en la que los nodos cooperan para propagar los datos entre sí de forma descentralizada. 
                
                A diferencia de las redes tradicionales (comunmente llamadas 'de estrella' o 'de árbol') donde el tráfico viaja a través de un único enrutador central de internet, en una red de malla los nodos se conectan entre sí de forma redundante y dinámica. 
                
                CARACTERÍSTICAS:
                1. Auto-configuración: Los nodos descubren automáticamente la ruta más óptima hacia el punto de acceso (Gateway) con internet más cercano.
                2. Auto-recuperación: Si un nodo intermedio se apaga o sale del rango, el tráfico se redirige instantáneamente a través de otro enlace dinámico cercano.
                3. Escalabilidad robusta: Entre más dispositivos se unan e instalen el navegador, mayor alcance, velocidad y redundancia constructiva tendrá la red en general.
                
                HISTORIA Y USO:
                Las tecnologías de malla fueron inicialmente concebidas para el ejército, pero hoy resultan vitales en misiones de búsqueda y rescate, zonas afectadas por catástrofes naturales, protestas con baja señal, y comunidades rurales desconectadas donde la infraestructura de fibra óptica convencional no llega. En Mesh Browser emulamos este protocolo utilizando túneles encriptados punto a punto para proteger cada salto de navegación de los intermediarios.
            """.trimIndent(),
            category = "Educación",
            sizeBytesCompressed = 8410,
            sizeBytesOriginal = 152000,
            hasImages = true
        ),
        LocalWebPage(
            url = "mesh.news",
            title = "Mesh News - Noticias Libres y Descentralizadas",
            textContent = """
                [URGENTE] COBERTURA EN EVENTOS MASIVOS:
                En el reciente festival del centro histórico, más de 20,000 personas sufrieron desconexión por la saturación de las torres de telefonía celular 4G/5G. Un grupo de 30 estudiantes instaló Mesh Browser, creando una malla que enlazó a más de 450 usuarios cercanos. De este modo, pudieron coordinar puntos de reunión y compartir un canal de texto con el mundo exterior totalmente sin cobros de datos corporativos.
                
                [TECNOLOGÍA] ENCRIPTACIÓN DE MALLA EXPANSIVA:
                Expertos en criptografía de la Universidad Estatal validaron los túneles locales de Mesh Browser que emplean criptografía de curva elíptica. Cada paquete de navegación viaja con cifrado simétrico extremo-a-extremo. Ninguno de los nodos intermediarios de la malla que relevan la señal (retransmisores) puede interceptar los datos privados de navegación del usuario final.
                
                [COMUNIDAD] LLEGAN NUEVOS NODOS EN ALTA MONTAÑA:
                La comunidad de San Mateo de las Piedras incrementó su cobertura de malla en un 300% gracias a la instalación de routers cargados con baterías solares. Ahora, un solo enlace de satélite compartido en la base provee de conectividad a 5 km a la redonda de forma cooperativa.
            """.trimIndent(),
            category = "Noticias",
            sizeBytesCompressed = 12050,
            sizeBytesOriginal = 310000,
            hasImages = true
        ),
        LocalWebPage(
            url = "local.forum",
            title = "Foro Comunitario - Conversaciones Locales",
            textContent = """
                ¡Bienvenido a la pizarra local de conexión! Este foro vive enteramente alojado en la memoria colectiva de los usuarios que te rodean en un radio de 50 metros.
                
                Últimos mensajes de la red de malla:
                ---
                [Carlos_M8] (a 3m): "Ofrezco mi enlace de Wi-Fi de alta velocidad en la esquina de la cafetería de 8:00 AM a 6:00 PM. ¡Activen compartir en sus dispositivos para extender la señal por el parque!"
                ---
                [Sara_P] (a 8m): "Hola, ¿alguien sabe si hay señal celular subiendo por el mirador? Uso este foro mesh porque allá arriba no hay cobertura 4G."
                ---
                [MeshRelay_Bot] (a 1m): "Estadística de Malla Activa: 4 nodos puente conectados. 582 KB relay de paquetes encriptados en los últimos 15 min."
                ---
                [Eduardo_G] (a 6m): "Probando la red desde el sótano del edificio de ingenierías. ¡Me llega internet de rebote! Increíble que con puros saltos dinámicos funcione."
            """.trimIndent(),
            category = "Comunidad",
            sizeBytesCompressed = 9500,
            sizeBytesOriginal = 85000
        ),
        LocalWebPage(
            url = "clima.node",
            title = "Clima Local y Alertas - Nodo Meteorológico",
            textContent = """
                REPORTE ACTUALIZADO - REGION CENTRAL:
                SITUACIÓN: Soleado con intervalos nubosos por la tarde.
                TEMPERATURA: 24 °C (Min: 14 °C, Max: 28 °C).
                ALERTA DE PRECIPITACIONES: Bajo riesgo de lluvias para el fin de semana.
                
                NOTA METEOROLÓGICA MÓVIL:
                Este nodo de clima se retransmite de forma automática y silenciosa cada 60 minutos entre todos los dispositivos que tengan activado 'Compartir Internet' o 'Compartir Datos' en un búfer circular de ultra-bajo consumo energético. Ideal para excursionistas y labores agrícolas sin acceso a servicios centralizados de meteorología.
            """.trimIndent(),
            category = "Clima",
            sizeBytesCompressed = 4120,
            sizeBytesOriginal = 45000
        )
    )

    fun getPage(url: String): LocalWebPage? {
        val cleanUrl = url.lowercase().trim().replace("https://", "").replace("http://", "").replace("www.", "")
        return PAGES.find { cleanUrl.contains(it.url) || it.url.contains(cleanUrl) }
    }

    fun makeDynamicPlaceholder(url: String): LocalWebPage {
        val cleanUrl = url.lowercase().trim().replace("https://", "").replace("http://", "").replace("www.", "")
        val titleName = cleanUrl.substringBefore("/").capitalize()
        
        // Simulates custom data compression on the fly
        val originalBytes = (100000..800000).random().toLong()
        val compressedBytes = (5000..15000).random().toLong()

        return LocalWebPage(
            url = cleanUrl,
            title = "$titleName (Optimizado por Malla Transcoder)",
            textContent = """
                INFORMACIÓN DEL SITIO ($cleanUrl):
                Este sitio web externo se cargó a través de la Red de Malla Descentralizada Mesh Browser. 
                
                DEBIDO A LA INTEGRIDAD DE TU CONEXIÓN:
                El contenido ha sido filtrado, comprimido y transcodificado de forma automática por el Nodo Gateway activo a 10 metros, eliminando scripts innecesarios, rastreadores pesados y renderizando solo texto legible para maximizar la velocidad.
                
                ---
                CONTENIDO SINTETIZADO DE LA PÁGINA:
                - Título de Enlace: Bienvenido a $titleName.
                - Estado de Conexión: Recibido por relevos encriptados de malla (Salto de Malla: Activo).
                - Privacidad Protegida: Criptografía Extremo a Extremo enrutable.
                - Consejos de Ahorro: Para cargar las imágenes pesadas de este sitio, toca el botón de 'Recargar Imágenes' si el ancho de banda del canal local es estable.
                
                Esta tecnología permite leer contenidos informativos esenciales incluso en condiciones extremas de baja cobertura o emergencias locales.
            """.trimIndent(),
            category = "Web Comprimida",
            sizeBytesCompressed = compressedBytes,
            sizeBytesOriginal = originalBytes,
            hasImages = true
        )
    }
}
