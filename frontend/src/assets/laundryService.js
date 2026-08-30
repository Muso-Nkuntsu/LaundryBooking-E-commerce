import { apiClient } from './apiClient';

/**
 * @typedef {Object} LaundryService
 * @property {string} id
 * @property {string} name
 * @property {string} description
 * @property {number} price
 * @property {string} [currency]      defaults to "ZAR" in the UI layer
 * @property {boolean} available
 * @property {string} [durationLabel] e.g. "45 min cycle"
 * @property {string} [category]      e.g. "wash" | "dry" | "add-on"
 */

/**
 * @param {string} residenceId
 * @returns {Promise<LaundryService[]>}
 */
export function getLaundryServices(residenceId, options) {
  return apiClient.get(`/residences/${residenceId}/laundry-services`, options);
}

/**
 * @param {string} serviceId
 * @returns {Promise<LaundryService>}
 */
export function getLaundryServiceById(serviceId, options) {
  return apiClient.get(`/laundry-services/${serviceId}`, options);
}

/**
 * Attach a laundry service to an in-progress booking.
 * @param {string} bookingId
 * @param {string} serviceId
 */
export function addServiceToBooking(bookingId, serviceId, options) {
  return apiClient.post(`/bookings/${bookingId}/services`, { serviceId }, options);
}

/**
 * Remove a previously added laundry service from an in-progress booking.
 * @param {string} bookingId
 * @param {string} serviceId
 */
export function removeServiceFromBooking(bookingId, serviceId, options) {
  return apiClient.delete(`/bookings/${bookingId}/services/${serviceId}`, options);
}