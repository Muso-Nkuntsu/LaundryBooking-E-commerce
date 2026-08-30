import { apiClient } from './apiClient';

/**
 * @typedef {Object} TimeSlot
 * @property {string} id
 * @property {string} date        ISO date, e.g. "2026-08-24"
 * @property {string} startTime   "08:00"
 * @property {string} endTime     "09:00"
 * @property {'available'|'booked'|'unavailable'} status
 * @property {string} [machineType]  e.g. "washer" | "dryer"
 */

/**
 * Dates that have at least one bookable slot for a residence.
 * @param {string} residenceId
 * @returns {Promise<{date: string, hasAvailability: boolean}[]>}
 */
export function getAvailableDates(residenceId, options) {
  return apiClient.get(`/residences/${residenceId}/timeslots/dates`, options);
}

/**
 * All slots (available + unavailable) for a residence on a given date,
 * so the UI can render booked slots as disabled rather than hiding them.
 * @param {string} residenceId
 * @param {string} date  ISO date "YYYY-MM-DD"
 * @returns {Promise<TimeSlot[]>}
 */
export function getTimeSlots(residenceId, date, options) {
  const query = new URLSearchParams({ date }).toString();
  return apiClient.get(`/residences/${residenceId}/timeslots?${query}`, options);
}

/**
 * Reserve a slot as part of the booking flow. Kept here (rather than in the
 * booking-confirmation module) since it operates directly on the TimeSlot resource.
 * @param {string} slotId
 */
export function holdTimeSlot(slotId, options) {
  return apiClient.post(`/timeslots/${slotId}/hold`, {}, options);
}