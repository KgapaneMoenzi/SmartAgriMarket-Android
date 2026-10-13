package com.smartagrimarket.model.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface ApiService {

    // ── HEALTH CHECK ──
    @GET("api/health")
    suspend fun healthCheck(): Response<HealthResponse>

    // ── AUTH ──
    @POST("api/auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("api/auth/resend-code")
    suspend fun resendCode(@Body request: ResendCodeRequest): Response<ResendCodeResponse>

    @POST("api/auth/signin")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") bearerToken: String): Response<Map<String, UserDto>>

    @PATCH("api/auth/me")
    suspend fun updateProfile(
        @Header("Authorization") bearerToken: String,
        @Body request: UpdateProfileRequest
    ): Response<UserDto>

    @PATCH("api/auth/me/deactivate")
    suspend fun deactivateAccount(
        @Header("Authorization") bearerToken: String
    ): Response<Unit>

    @DELETE("api/auth/me")
    suspend fun deleteAccount(@Header("Authorization") bearerToken: String): Response<Unit>

    @Multipart
    @POST("api/auth/me/photo")
    suspend fun uploadProfilePhoto(
        @Header("Authorization") bearerToken: String,
        @Part photo: okhttp3.MultipartBody.Part
    ): Response<UpdateProfilePhotoResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(
        @Header("Authorization") bearerToken: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @GET("api/auth/me/address")
    suspend fun getAddress(@Header("Authorization") bearerToken: String): Response<AddressDto>

    @PUT("api/auth/me/address")
    suspend fun updateAddress(
        @Header("Authorization") bearerToken: String,
        @Body request: AddressRequest
    ): Response<AddressDto>

    @GET("api/auth/me/farm-profile")
    suspend fun getFarmProfile(@Header("Authorization") bearerToken: String): Response<FarmProfileDto>

    @PUT("api/auth/me/farm-profile")
    suspend fun updateFarmProfile(
        @Header("Authorization") bearerToken: String,
        @Body request: FarmProfileDto
    ): Response<FarmProfileDto>

    @POST("api/auth/me/export")
    suspend fun requestDataExport(@Header("Authorization") bearerToken: String): Response<DataExportResponse>

    @GET("api/auth/notification-settings")
    suspend fun getNotificationSettings(@Header("Authorization") bearerToken: String): Response<NotificationSettingsDto>

    @PUT("api/auth/notification-settings")
    suspend fun updateNotificationSettings(
        @Header("Authorization") bearerToken: String,
        @Body request: NotificationSettingsDto
    ): Response<NotificationSettingsDto>

    // ── PAYMENTS ──
    @POST("api/payments/create")
    suspend fun createPayment(@Body request: PaymentCreateRequest): Response<PaymentCreateResponse>

    @POST("api/delivery/quote")
    suspend fun getDeliveryQuote(
        @Header("Authorization") bearerToken: String,
        @Body body: DeliveryQuoteRequest
    ): Response<DeliveryQuoteResponse>

    // ── PRODUCTS ──
    @GET("api/products")
    suspend fun getProducts(): Response<ProductListResponse>

    @POST("api/products/cart-validate")
    suspend fun validateCart(@Body request: CartValidateRequest): Response<CartValidateResponse>

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") bearerToken: String,
        @Body request: CreateProductRequest
    ): Response<CreateProductResponse>

    @Multipart
    @POST("api/products/upload-image")
    suspend fun uploadProductImage(
        @Header("Authorization") bearerToken: String,
        @Part image: okhttp3.MultipartBody.Part
    ): Response<ImageUploadResponse>

    @GET("api/products/my")
    suspend fun getMyProducts(
        @Header("Authorization") bearerToken: String
    ): Response<ProductListResponse>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<Unit>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body request: UpdateProductRequest
    ): Response<CreateProductResponse>

    @POST("api/products/{id}/view")
    suspend fun logProductView(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<Map<String, Boolean>>

    // ── ORDERS ──
    @GET("api/orders")
    suspend fun getOrders(@Header("Authorization") bearerToken: String): Response<OrdersListResponse>

    @GET("api/orders/{id}")
    suspend fun getOrderDetail(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<OrderDetailResponse>

    @GET("api/orders/{id}/driver-location")
    suspend fun getDriverLocation(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<DriverLocationResponse>

    @GET("api/orders/{id}/route")
    suspend fun getOrderRoute(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<RouteResponse>

    // ── RECOMMENDATIONS ──
    @GET("api/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") bearerToken: String
    ): Response<RecommendationsResponse>

    @GET("api/recommendations/similar/{productId}")
    suspend fun getSimilarProducts(
        @Path("productId") productId: String
    ): Response<RecommendationsResponse>

    @GET("api/recommendations/trending")
    suspend fun getTrendingProducts(): Response<RecommendationsResponse>

    // ── WEATHER ──
    @GET("api/weather/advice")
    suspend fun getWeatherAdvice(
        @Header("Authorization") bearerToken: String,
        @Query("location") location: String
    ): Response<FarmerWeatherAdvice>

    // ── FARMER ──
    @GET("api/farmer/stats")
    suspend fun getFarmerStats(
        @Header("Authorization") bearerToken: String
    ): Response<FarmerStatsDto>

    @GET("api/farmer/orders")
    suspend fun getFarmerOrders(
        @Header("Authorization") bearerToken: String
    ): Response<FarmerOrdersListResponse>

    @GET("api/farmer/orders/{id}")
    suspend fun getFarmerOrderDetail(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<FarmerOrderDetailResponse>

    @PATCH("api/farmer/orders/{id}/status")
    suspend fun updateFarmerOrderStatus(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body body: FarmerOrderStatusUpdate
    ): Response<FarmerOrderDetailResponse>

    @GET("api/farmer/stats/extended")
    suspend fun getFarmerStatsExtended(
        @Header("Authorization") bearerToken: String
    ): Response<FarmerStatsExtendedDto>

    @GET("api/farmer/crop-recommendations")
    suspend fun getCropRecommendations(
        @Header("Authorization") bearerToken: String
    ): Response<CropRecommendationsResponse>

    @POST("api/farmer/crop-recommendations/{crop}/dismiss")
    suspend fun dismissCropRecommendation(
        @Header("Authorization") bearerToken: String,
        @Path("crop") crop: String
    ): Response<Map<String, Boolean>>

    // ── REVIEWS / Q&A ──
    @GET("api/products/{productId}/reviews")
    suspend fun getProductReviews(
        @Path("productId") productId: String
    ): Response<ReviewsListResponse>

    @GET("api/products/{productId}/reviews/eligibility")
    suspend fun getReviewEligibility(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String
    ): Response<ReviewEligibilityResponse>

    @POST("api/products/{productId}/reviews")
    suspend fun createReview(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String,
        @Body request: CreateReviewRequest
    ): Response<CreateReviewResponse>

    @GET("api/products/{productId}/questions")
    suspend fun getProductQuestions(
        @Path("productId") productId: String
    ): Response<QuestionsListResponse>

    @POST("api/products/{productId}/questions")
    suspend fun createQuestion(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String,
        @Body request: CreateQuestionRequest
    ): Response<CreateQuestionResponse>

    // ── NOTIFICATIONS ──
    @GET("api/notifications")
    suspend fun getNotifications(
        @Header("Authorization") bearerToken: String
    ): Response<NotificationsListResponse>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationCount(
        @Header("Authorization") bearerToken: String
    ): Response<UnreadCountResponse>

    @PATCH("api/notifications/{id}/read")
    suspend fun markNotificationRead(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<Map<String, String>>

    @PATCH("api/notifications/read-all")
    suspend fun markAllNotificationsRead(
        @Header("Authorization") bearerToken: String
    ): Response<Map<String, String>>

    // ── DELIVERY SLOTS ──
    @GET("api/delivery-slots/{orderId}")
    suspend fun getDeliverySlots(
        @Header("Authorization") bearerToken: String,
        @Path("orderId") orderId: String
    ): Response<DeliverySlotsResponse>

    @POST("api/delivery-slots/{orderId}/select")
    suspend fun selectDeliverySlot(
        @Header("Authorization") bearerToken: String,
        @Path("orderId") orderId: String,
        @Body body: SelectSlotRequest
    ): Response<SelectSlotResponse>

    // ── BACK IN STOCK ──
    @POST("api/back-in-stock")
    suspend fun subscribeBackInStock(
        @Header("Authorization") bearerToken: String,
        @Body body: BackInStockRequest
    ): Response<BackInStockResponse>

    @GET("api/back-in-stock/{productId}")
    suspend fun checkBackInStock(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String
    ): Response<BackInStockResponse>

    // ── FARMER QUESTIONS ──
    @GET("api/farmer/questions")
    suspend fun getFarmerQuestions(
        @Header("Authorization") bearerToken: String
    ): Response<FarmerQuestionsListResponse>

    @PATCH("api/farmer/questions/{id}/answer")
    suspend fun answerQuestion(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body body: AnswerQuestionRequest
    ): Response<AnswerQuestionResponse>

    // ── LOYALTY / REFERRALS ──
    @GET("api/loyalty/me")
    suspend fun getLoyaltyMe(
        @Header("Authorization") bearerToken: String
    ): Response<LoyaltyMeResponse>

    @POST("api/loyalty/redeem")
    suspend fun redeemPoints(
        @Header("Authorization") bearerToken: String,
        @Body body: RedeemRequest
    ): Response<RedeemResponse>

    @GET("api/referrals/me")
    suspend fun getReferralsMe(
        @Header("Authorization") bearerToken: String
    ): Response<ReferralsMeResponse>

    // ── HELP ──
    @GET("api/help/articles")
    suspend fun getHelpArticles(
        @Query("audience") audience: String
    ): Response<HelpArticlesResponse>

    @GET("api/help/status")
    suspend fun getSupportStatus(): Response<SupportStatusResponse>

    @GET("api/help/chat")
    suspend fun getSupportChat(
        @Header("Authorization") bearerToken: String
    ): Response<SupportChatResponse>

    @POST("api/help/chat")
    suspend fun sendSupportMessage(
        @Header("Authorization") bearerToken: String,
        @Body body: SendSupportMessageRequest
    ): Response<SendSupportMessageResponse>

    // ── SEASONAL ──
    @GET("api/seasonal")
    suspend fun getSeasonal(
        @Query("month") month: Int,
        @Query("region") region: String
    ): Response<SeasonalResponse>

    // ── PRODUCT REQUESTS ──
    @POST("api/requests")
    suspend fun createRequest(
        @Header("Authorization") bearerToken: String,
        @Body body: CreateProductRequestBody
    ): Response<ProductRequestResponse>

    @GET("api/requests/mine")
    suspend fun getMyRequests(
        @Header("Authorization") bearerToken: String
    ): Response<ProductRequestListResponse>

    @GET("api/requests/farmers")
    suspend fun getFarmersForRequest(
        @Header("Authorization") bearerToken: String
    ): Response<FarmersListResponse>

    @GET("api/requests/farmer")
    suspend fun getFarmerRequests(
        @Header("Authorization") bearerToken: String
    ): Response<ProductRequestListResponse>

    @PATCH("api/requests/{id}/reply")
    suspend fun replyToRequest(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body body: ReplyRequestBody
    ): Response<ProductRequestResponse>

    @PATCH("api/requests/{id}/fulfilled")
    suspend fun markRequestFulfilled(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<ProductRequestResponse>

    // ── FAVORITES ──
    @GET("api/favorites")
    suspend fun getFavorites(
        @Header("Authorization") bearerToken: String
    ): Response<FavoritesListResponse>

    @GET("api/favorites/{productId}/check")
    suspend fun checkFavorite(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String
    ): Response<FavoriteStatusResponse>

    @POST("api/favorites/{productId}")
    suspend fun addFavorite(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String
    ): Response<FavoriteStatusResponse>

    @DELETE("api/favorites/{productId}")
    suspend fun removeFavorite(
        @Header("Authorization") bearerToken: String,
        @Path("productId") productId: String
    ): Response<FavoriteStatusResponse>

    // ── HOME BANNER ──
    @GET("api/home-banner")
    suspend fun getHomeBanner(
        @Header("Authorization") bearerToken: String
    ): Response<HomeBannerResponse>

    // ── FARMER VERIFICATION ──
    @Multipart
    @POST("api/verification/submit")
    suspend fun submitVerification(
        @Header("Authorization") bearerToken: String,
        @Part idDocument: MultipartBody.Part,
        @Part certificate: MultipartBody.Part,
        @Part("farmName") farmName: RequestBody,
        @Part("farmTown") farmTown: RequestBody,
        @Part("farmPrimaryCrops") farmPrimaryCrops: RequestBody,
        @Part("farmSizeHectares") farmSizeHectares: RequestBody,
        @Part("phone") phone: RequestBody
    ): Response<VerificationSubmitResponse>

    @GET("api/verification/me")
    suspend fun getVerificationStatus(
        @Header("Authorization") bearerToken: String
    ): Response<VerificationStatusResponse>

    // ── FARMER WALLET ──
    @GET("api/farmer/wallet")
    suspend fun getWallet(
        @Header("Authorization") bearerToken: String
    ): Response<WalletResponse>

    @POST("api/farmer/withdraw")
    suspend fun requestWithdrawal(
        @Header("Authorization") bearerToken: String,
        @Body request: WithdrawRequest
    ): Response<WithdrawResponse>

    // ── DRIVER ──
    @GET("api/driver/jobs")
    suspend fun getDriverJobs(
        @Header("Authorization") bearerToken: String
    ): Response<DriverJobsResponse>

    @POST("api/driver/jobs/{id}/accept")
    suspend fun acceptDriverJob(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<DriverJobResponse>

    @POST("api/driver/jobs/{id}/skip")
    suspend fun skipDriverJob(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String
    ): Response<Map<String, Boolean>>

    @PATCH("api/driver/jobs/{id}/status")
    suspend fun updateDriverJobStatus(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body body: DriverStatusUpdate
    ): Response<DriverJobResponse>

    @PATCH("api/driver/jobs/{id}/location")
    suspend fun updateDriverLocation(
        @Header("Authorization") bearerToken: String,
        @Path("id") id: String,
        @Body body: DriverLocationUpdate
    ): Response<Map<String, Boolean>>

    @PATCH("api/driver/availability")
    suspend fun setDriverAvailability(
        @Header("Authorization") bearerToken: String,
        @Body body: DriverAvailabilityRequest
    ): Response<DriverAvailabilityResponse>

    @GET("api/driver/stats")
    suspend fun getDriverStats(
        @Header("Authorization") bearerToken: String
    ): Response<DriverStatsDto>

    @GET("api/driver/zones")
    suspend fun getDriverZones(
        @Header("Authorization") bearerToken: String
    ): Response<ZonesResponse>

    @PATCH("api/driver/zones")
    suspend fun saveDriverZones(
        @Header("Authorization") bearerToken: String,
        @Body body: ZonesSaveRequest
    ): Response<ZonesResponse>

    @GET("api/driver/batch")
    suspend fun getDriverBatch(
        @Header("Authorization") bearerToken: String
    ): Response<BatchDto>

    // ── Driver batch lifecycle ──
    @POST("api/driver/batch/start")
    suspend fun startDriverBatch(
        @Header("Authorization") bearerToken: String
    ): Response<BatchActionResponse>

    @POST("api/driver/batch/collect")
    suspend fun collectDriverBatch(
        @Header("Authorization") bearerToken: String
    ): Response<BatchActionResponse>

    @POST("api/driver/batch/deliver/{deliveryId}")
    suspend fun deliverDriverStop(
        @Header("Authorization") bearerToken: String,
        @Path("deliveryId") deliveryId: String
    ): Response<BatchActionResponse>

    @GET("api/driver/wallet")
    suspend fun getDriverWallet(
        @Header("Authorization") bearerToken: String
    ): Response<DriverWalletDto>

    @POST("api/driver/withdraw")
    suspend fun requestDriverWithdrawal(
        @Header("Authorization") bearerToken: String,
        @Body request: DriverWithdrawRequest
    ): Response<DriverWithdrawResponse>

    @Multipart
    @POST("api/driver/verification/submit")
    suspend fun submitDriverVerification(
        @Header("Authorization") bearerToken: String,
        @Part licence: MultipartBody.Part,
        @Part permit: MultipartBody.Part,
        @Part registration: MultipartBody.Part,
        @Part carImage: MultipartBody.Part,
        @Part carImage2: MultipartBody.Part?,
        @Part carImage3: MultipartBody.Part?,
        @Part("vehicleMake") vehicleMake: RequestBody,
        @Part("vehicleModel") vehicleModel: RequestBody,
        @Part("vehiclePlate") vehiclePlate: RequestBody,
        @Part("vehicleColor") vehicleColor: RequestBody,
        @Part("addressLine1") addressLine1: RequestBody,
        @Part("addressLine2") addressLine2: RequestBody,
        @Part("addressCity") addressCity: RequestBody,
        @Part("addressProvince") addressProvince: RequestBody,
        @Part("addressPostalCode") addressPostalCode: RequestBody
    ): Response<DriverVerificationSubmitResponse>

    @GET("api/driver/verification/me")
    suspend fun getDriverVerification(
        @Header("Authorization") bearerToken: String
    ): Response<DriverVerificationMeResponse>
}

// ═══════════════════════════════════════════════════════════
// REQUEST / RESPONSE DATA CLASSES
// ═══════════════════════════════════════════════════════════

data class ForgotPasswordRequest(val email: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPassword: String)
data class HealthResponse(val ok: Boolean, val at: String)

data class UpdateProductRequest(
    val name: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val imageUrl: String? = null
)

data class CartValidateItem(
    val productId: String,
    val quantity: Int? = null,
    val name: String? = null,
)

data class CartValidateRequest(
    val items: List<CartValidateItem>,
)

data class CartUnavailableItem(
    val productId: String,
    val name: String = "",
    val reason: String = "",
    val availableStock: Int? = null,
)

data class CartValidateResponse(
    val unavailable: List<CartUnavailableItem> = emptyList(),
)

data class DeliveryQuoteItem(
    val farmerEmail: String? = null,
    val productId: String? = null,
)

data class DeliveryQuoteRequest(
    val items: List<DeliveryQuoteItem>,
    val latitude: Double,
    val longitude: Double,
)

data class DeliveryQuoteResponse(
    val fee: Double? = null,
    val distanceKm: Double? = null,
    val tier: String? = null,
    val fallback: Boolean = false,
    val reason: String? = null,
)

data class DriverLocationUpdate(
    val latitude: Double,
    val longitude: Double
)

data class DriverLocationResponse(
    val orderId: String? = null,
    val trackingStatus: String? = null,
    val deliveryStatus: String? = null,
    val driverName: String? = null,
    val driverPlate: String? = null,
    val driverPhone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val updatedAt: String? = null
)

data class RouteResponse(
    val available: Boolean = false,
    val reason: String? = null,
    val distanceMeters: Double? = null,
    val durationSeconds: Double? = null,
    val polyline: List<List<Double>> = emptyList()
)

data class RecommendedProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String? = null,
    val stock: Int? = null,
    val farmName: String? = null,
    val farmerEmail: String? = null,
    val location: String? = null,
)

data class RecommendationsResponse(
    val generatedAt: String? = null,
    val source: String? = null,
    val products: List<RecommendedProductDto> = emptyList(),
)

data class CropRecommendationDto(
    val crop: String,
    val score: Int = 0,
    val reason: String = "",
    val demand: Int = 0,
)

data class CropRecommendationsResponse(
    val month: Int? = null,
    val region: String? = null,
    val recommendations: List<CropRecommendationDto> = emptyList(),
)

data class DeliverySlotDto(
    val id: String,
    val label: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val available: Boolean = true
)

data class DeliverySlotsResponse(
    val orderId: String? = null,
    val slots: List<DeliverySlotDto> = emptyList(),
    val selectedSlotId: String? = null
)

data class SelectSlotRequest(
    val start: String,
    val end: String
)

data class SelectSlotResponse(
    val ok: Boolean = true,
    val message: String? = null,
    val selectedSlotId: String? = null
)

data class BackInStockRequest(
    val productId: String
)

data class BackInStockResponse(
    val subscribed: Boolean = false,
    val productId: String? = null,
    val message: String? = null
)