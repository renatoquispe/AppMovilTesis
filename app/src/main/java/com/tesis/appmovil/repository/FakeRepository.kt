package com.tesis.appmovil.repository

import com.tesis.appmovil.models.Service

class FakeRepository {
    fun getOffers(): List<Service> = listOf(
        Service(
            id = "1",
            title = "Corte de cabello",
            businessName = "Barbería Cúspide",
            duration = "30min",
            price = 20.0,
            imageUrl = "https://ayalatin.com/docs/6bca84cd.jpg",
            tag = "Barbería"
        ),
        Service(
            id = "2",
            title = "Manicure y Pedicure",
            businessName = "Rapid Nails",
            duration = "1h",
            price = 50.0,
            imageUrl = "https://facesspa.com/wp-content/uploads/2020/05/AdobeStock_56635333.jpeg",
            tag = "Manicure"
        ),
        Service(
            id = "2",
            title = "Cejas y pestañas",
            businessName = "Rapid Nails",
            duration = "1h",
            price = 50.0,
            imageUrl = "https://hercomvenezuela.com/wp-content/uploads/2021/06/cejas-y-pestanas.jpg",
            tag = "Manicure"
        )
    )

    fun getStyles(): List<Service> = listOf(
        Service(
            id = "3",
            title = "UÑAS ACRÍLICAS",
            businessName = "ROYALTY BARBERSHOP",
            duration = "Abierto ahora · 08:00–14:00",
            price = 0.0,
            imageUrl = "https://uvn-brightspot.s3.amazonaws.com/assets/vixes/imj/imujer/M/Modelos-de-unas-acrilicas-2.jpg",
            rating = 4.5,
            tag = "Barbería",
            location = "Independencia, Lima"
        ),
        Service(
            id = "4",
            title = "CORTE FADE",
            businessName = "JENCY CREANDO ESTILOS",
            duration = "Abierto ahora · 08:00–14:00",
            price = 0.0,
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSSe9BEyFWg4MAEPNM1T6D_cZPCYGzrno29RQ&s",
            rating = 4.2,
            tag = "Peluquería",
            location = "Comas, Lima"
        ),
        Service(
            id = "4",
            title = "BALAYAGE",
            businessName = "JENCY CREANDO ESTILOS",
            duration = "Abierto ahora · 08:00–14:00",
            price = 0.0,
            imageUrl = "https://cdn.shopify.com/s/files/1/0790/1003/8107/files/SCOPRI_LA_SELEZIONE_DI_SHAMPOO_SEBOEQUILIBRANTI_TESTATI_NEL_NOSTRO_SALONE._300_x_300_px_1_480x480.png?v=1736290646",
            rating = 4.2,
            tag = "Peluquería",
            location = "Comas, Lima"
        )
    )

    fun getServices(): List<Service> = listOf(
        Service(
            id = "5",
            title = "Corte de cabello",
            businessName = "ROYALTY BARBERSHOP",
            duration = "30–45min",
            price = 19.9,
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS26q87KhNDkwELu64OD4eHorEICzDigdKMk2vxw_9vwtYCEF1CEitrWGqa0zjPfmjZfuE&usqp=CAU",
            rating = 4.6,
            tag = "Peluquería"
        ),
        Service(
            id = "6",
            title = "Alineación + corte",
            businessName = "Barber Pro",
            duration = "1h",
            price = 29.9,
            imageUrl = "https://d375139ucebi94.cloudfront.net/region2/es/122346/biz_photo/fbf61d5c07324297a67bc1dfc639ad-elegant-barber-shop-biz-photo-6b263cb46a7549a395b695bf60cd9c-booksy.jpeg?size=640x427",
            rating = 4.7,
            tag = "Barbería"
        ),
        Service(
            id = "6",
            title = "Alineación + corte",
            businessName = "Barber Pro",
            duration = "1h",
            price = 29.9,
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ66LBo9zZS3bY9gJXLH8IayFJHrqAjEWM89c5t-l7zYfWn3FEnIxTQpaFObWitOabXlWU&usqp=CAU",
            rating = 4.7,
            tag = "Barbería"
        )
    )
}