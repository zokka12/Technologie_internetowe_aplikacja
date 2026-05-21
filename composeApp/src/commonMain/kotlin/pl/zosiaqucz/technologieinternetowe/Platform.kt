package pl.zosiaqucz.technologieinternetowe

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform