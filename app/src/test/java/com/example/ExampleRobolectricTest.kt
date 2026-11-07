package com.example

import com.example.data.model.AdCampaignEntity
import com.example.data.model.UserEntity
import com.example.data.model.VetaRemoteConfig
import com.example.data.repository.SocialRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify Super Admin access is strictly restricted to VaanijSingh28@gmail_com`() {
        val regularCreator = UserEntity(
            id = "u_ava",
            username = "AvaGlow",
            fullName = "Ava Sterling",
            email = "ava.sterling@gmail.com",
            isGmailVerified = true,
            bio = "Creator",
            avatarUrl = "",
            followersCount = 15000,
            followingCount = 100,
            postsCount = 10,
            role = "USER"
        )
        assertFalse(SocialRepository.isVerifiedSuperAdmin(regularCreator))

        val spoofedEmailUser = regularCreator.copy(email = "VaanijSingh28@gmail.com", role = "USER")
        assertFalse(SocialRepository.isVerifiedSuperAdmin(spoofedEmailUser))

        val superAdmin = UserEntity(
            id = SocialRepository.SUPER_ADMIN_UID,
            username = "VaanijSingh28",
            fullName = "Vaanij Singh (VETA Founder)",
            email = SocialRepository.SUPER_ADMIN_EMAIL,
            isGmailVerified = true,
            bio = "Super Admin",
            avatarUrl = "",
            followersCount = 248000,
            followingCount = 18,
            postsCount = 12,
            role = "SUPER_ADMIN"
        )
        assertTrue(SocialRepository.isVerifiedSuperAdmin(superAdmin))
    }

    @Test
    fun `verify Spotpix by VETA remote thresholds and vanij7_fam UPI settlement`() {
        val config = VetaRemoteConfig()
        assertEquals(1000, config.blueTickMinFollowers)
        assertEquals(1100, config.monetizationMinFollowers)
        assertEquals(10_000_000L, config.monetizationMinReelViews90d)
        assertEquals(100, config.weeklyAdPriceInr)
        assertEquals(120, config.twoWeekAdPriceInr)
        assertEquals("vanij7@fam", SocialRepository.MERCHANT_SETTLEMENT_UPI)

        val weeklyCampaign = AdCampaignEntity(
            id = "camp_test_weekly",
            ownerId = "u_me",
            ownerUsername = "AlexRivera",
            title = "Spotpix Weekly Promo",
            caption = "Weekly campaign test",
            creativeUrlsCsv = "https://example.com/1.jpg",
            packageType = "WEEKLY",
            durationDays = 7,
            amountInr = config.weeklyAdPriceInr
        )
        assertEquals("PENDING_PAYMENT", weeklyCampaign.status)
        assertEquals(100, weeklyCampaign.amountInr)
    }
}
