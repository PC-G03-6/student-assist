/**
 * Modulul de transport în comun: proxy către API-ul Tranzy (stații, linii,
 * vehicule, sosiri), cache pentru răspunsuri și rutarea spre clădiri.
 * Cheia API se citește doar din configurare (variabila de mediu TRANZY_API_KEY),
 * niciodată hardcodată.
 */
package ro.studentassist.transit;