package com.tesis.appmovil.data

class FakeRepository {
    fun getNearbyStyles(): List<Service> = listOf(
        Service(
            id = "1",
            title = "Corte fade",
            businessName = "Barbería Cúspide",
            duration = "30min",
            price = 20.0,
            imageUrl = "https://picsum.photos/seed/fade/400/300",
            tag = "Barbería"
        ),
        Service(
            id = "2",
            title = "Uñas acrílicas",
            businessName = "Rapid Nails",
            duration = "1h",
            price = 50.0,
            imageUrl = "https://picsum.photos/seed/nails/400/300",
            tag = "Manicure"
        )
    )

    fun getFeatured(): List<Service> = listOf(
        Service(
            id = "3",
            title = "ROYALTY BARBERSHOP",
            businessName = "ROYALTY BARBERSHOP",
            duration = "Abierto ahora · 08:00–14:00",
            price = 0.0,
            imageUrl = "https://picsum.photos/seed/royalty/800/600",
            rating = 4.5,
            tag = "Barbería",
            location = "Independencia, Lima"
        ),
        Service(
            id = "4",
            title = "JENCY CREANDO EST...",
            businessName = "JENCY CREANDO ESTILOS",
            duration = "Abierto ahora · 08:00–14:00",
            price = 0.0,
            imageUrl = "https://picsum.photos/seed/jency/800/600",
            rating = 4.2,
            tag = "Peluquería",
            location = "Comas, Lima"
        )
    )

    fun getDeals(): List<Service> = listOf(
        Service(
            id = "5",
            title = "Corte de cabello",
            businessName = "ROYALTY BARBERSHOP",
            duration = "30–45min",
            price = 19.9,
            imageUrl = "https://picsum.photos/seed/deal1/800/600",
            rating = 4.6,
            tag = "Oferta"
        ),
        Service(
            id = "6",
            title = "Alineación + corte",
            businessName = "Barber Pro",
            duration = "1h",
            price = 29.9,
            imageUrl = "https://picsum.photos/seed/deal2/800/600",
            rating = 4.7,
            tag = "Oferta"
        )
    )
}
