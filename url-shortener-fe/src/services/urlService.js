import api from './api'

export function createShortUrl(originalUrl){
  return api.post('/api/shorten',{
    url: originalUrl,
  })
}
export function getStats(shortCode){
  return api.get(`/api/stats/${shortCode}`)
}