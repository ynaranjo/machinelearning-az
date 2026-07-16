package com.kidsguard.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainMatcherTest {

    // ---------- normalize ----------

    @Test
    fun `normalize quita esquema, www, ruta y query`() {
        assertEquals("ejemplo.com", DomainMatcher.normalize("https://www.ejemplo.com/ruta?x=1"))
        assertEquals("ejemplo.com", DomainMatcher.normalize("http://ejemplo.com"))
        assertEquals("ejemplo.com", DomainMatcher.normalize("  EJEMPLO.com  "))
        assertEquals("sub.ejemplo.com", DomainMatcher.normalize("sub.ejemplo.com/algo"))
    }

    // ---------- matches ----------

    @Test
    fun `matches exacto y subdominios`() {
        assertTrue(DomainMatcher.matches("ejemplo.com", "ejemplo.com"))
        assertTrue(DomainMatcher.matches("www.ejemplo.com", "ejemplo.com"))
        assertTrue(DomainMatcher.matches("videos.ejemplo.com", "ejemplo.com"))
        assertTrue(DomainMatcher.matches("a.b.ejemplo.com", "ejemplo.com"))
    }

    @Test
    fun `matches no confunde dominios parecidos`() {
        assertFalse(DomainMatcher.matches("noejemplo.com", "ejemplo.com"))
        assertFalse(DomainMatcher.matches("ejemplo.com.evil.com", "ejemplo.com"))
        assertFalse(DomainMatcher.matches("ejemplo.org", "ejemplo.com"))
    }

    @Test
    fun `matches con dominio vacio es falso`() {
        assertFalse(DomainMatcher.matches("ejemplo.com", ""))
        assertFalse(DomainMatcher.matches("ejemplo.com", "   "))
    }

    // ---------- isAllowed: lista negra ----------

    @Test
    fun `lista negra permite todo salvo lo bloqueado`() {
        val blocked = setOf("malo.com")
        assertTrue(DomainMatcher.isAllowed("bueno.com", false, blocked, emptySet()))
        assertFalse(DomainMatcher.isAllowed("malo.com", false, blocked, emptySet()))
        assertFalse(DomainMatcher.isAllowed("sub.malo.com", false, blocked, emptySet()))
    }

    // ---------- isAllowed: lista blanca ----------

    @Test
    fun `lista blanca solo permite lo listado`() {
        val allowed = setOf("ok.com", "wikipedia.org")
        assertTrue(DomainMatcher.isAllowed("ok.com", true, emptySet(), allowed))
        assertTrue(DomainMatcher.isAllowed("es.wikipedia.org", true, emptySet(), allowed))
        assertFalse(DomainMatcher.isAllowed("otro.com", true, emptySet(), allowed))
    }

    @Test
    fun `lista blanca respeta el bloqueo aunque este permitido`() {
        val allowed = setOf("ejemplo.com")
        val blocked = setOf("malo.ejemplo.com")
        assertTrue(DomainMatcher.isAllowed("bueno.ejemplo.com", true, blocked, allowed))
        assertFalse(DomainMatcher.isAllowed("malo.ejemplo.com", true, blocked, allowed))
    }
}
