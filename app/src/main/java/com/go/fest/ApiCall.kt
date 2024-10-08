package com.go.fest

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class Festival(
    val nom_du_festival: String?,
    val envergure_territoriale: String?,
    val region_principale_de_deroulement: String?,
    val departement_principal_de_deroulement: String?,
    val commune_principale_de_deroulement: String?,
    val code_postal_de_la_commune_principale_de_deroulement: String?,
    val code_insee_commune: String?,
    val code_insee_epci_collage_en_valeur: String?,
    val libelle_epci_collage_en_valeur: String?,
    val numero_de_voie: String?,
    val type_de_voie_rue_avenue_boulevard_etc: String?,
    val nom_de_la_voie: String?,
    val adresse_postale: String?,
    val complement_d_adresse_facultatif: String?,
    val site_internet_du_festival: String?,
    val adresse_e_mail: String?,
    val decennie_de_creation_du_festival: String?,
    val annee_de_creation_du_festival: String?,
    val discipline_dominante: String?,
    val sous_categorie_spectacle_vivant: String?,
    val sous_categorie_musique: String?,
    val sous_categorie_musique_cnm: String?,
    val sous_categorie_cinema_et_audiovisuel: String?,
    val sous_categorie_arts_visuels_et_arts_numeriques: String?,
    val sous_categorie_livre_et_litterature: String?,
    val periode_principale_de_deroulement_du_festival: String?,
    val identifiant_agence_a: String?,
    val identifiant: String?,
    val geocodage_xy: GeocodageXY?
)

data class GeocodageXY(
    val lon: Double?,
    val lat: Double?
)

data class FestivalResponse(
    val results: List<Festival>
)

interface FestivalApiService {

    /**
     * Retrieve a list of festivals
     *
     * @param limit The maximum number of results to return
     * @param offset The offset from the start of the results
     * @param refine A query parameter to filter results
     * @return [FestivalResponse] containing a list of festivals
     */
    @GET("api/explore/v2.1/catalog/datasets/festivals-global-festivals-_-pl/records")
    suspend fun getFestivals(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("refine") refine: String? = null
    ): FestivalResponse

    /**
     * Retrieve single festival based on a where (generally by id)
     *
     * @param whereClause The condition to filter the festival
     * @param limit The maximum number of results to return (default is 1)
     * @return [FestivalResponse] containing the festival that matches the condition
     */
    @GET("api/explore/v2.1/catalog/datasets/festivals-global-festivals-_-pl/records")
    suspend fun getOneFestival(
        @Query("where") whereClause: String,
        @Query("limit") limit: Int = 1
    ): FestivalResponse
}

/**
 * Object for creating the Retrofit client and access Festival API service
 */
object ApiClient {
    private const val BASE_URL = "https://data.culture.gouv.fr/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val festivalApiService: FestivalApiService by lazy {
        retrofit.create(FestivalApiService::class.java)
    }
}
