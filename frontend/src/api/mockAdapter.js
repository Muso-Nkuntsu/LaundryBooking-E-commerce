/**
 * Local dev mock adapter. Enabled by setting VITE_USE_MOCKS=true in .env
 * (see .env.example). Lets the screens run and be demoed before the real
 * TimeSlot / LaundryService endpoints are wired up — remove once the
 * backend is live.
 */

function isoDate(offsetDays) {
  const d = new Date();
  d.setDate(d.getDate() + offsetDays);
  return d.toISOString().slice(0, 10);
}

const MOCK_DATES = Array.from({ length: 6 }, (_, i) => ({
  date: isoDate(i),
  hasAvailability: i !== 2, // day index 2 is fully booked, to demo the "Full" state
}));

function buildSlotsForDate(date, fullyBooked) {
  const times = [
    ['07:00', '08:00'],
    ['08:00', '09:00'],
    ['09:00', '10:00'],
    ['17:00', '18:00'],
    ['18:00', '19:00'],
    ['19:00', '20:00'],
  ];
  return times.map(([startTime, endTime], i) => ({
    id: `${date}-slot-${i}`,
    date,
    startTime,
    endTime,
    status: fullyBooked ? 'booked' : i % 3 === 1 ? 'booked' : 'available',
    machineType: i % 2 === 0 ? 'washer' : 'dryer',
  }));
}

const MOCK_SERVICES = [
  {
    id: 'svc-wash-standard',
    name: 'Standard Wash',
    description: 'A full machine wash cycle using the residence-provided detergent, sized for one laundry bag.',
    price: 25,
    currency: 'ZAR',
    available: true,
    durationLabel: '40 min cycle',
    category: 'wash',
  },
  {
    id: 'svc-dry-standard',
    name: 'Standard Dry',
    description: 'A full tumble-dry cycle, recommended straight after a standard wash.',
    price: 20,
    currency: 'ZAR',
    available: true,
    durationLabel: '35 min cycle',
    category: 'dry',
  },
  {
    id: 'svc-wash-delicate',
    name: 'Delicate Wash',
    description: 'A gentle, cold-water cycle for delicates and items that need lower spin speeds.',
    price: 30,
    currency: 'ZAR',
    available: true,
    durationLabel: '50 min cycle',
    category: 'wash',
  },
  {
    id: 'svc-fold',
    name: 'Fold & Bag',
    description: 'Your laundry folded and bagged for pickup after the wash and dry cycles finish.',
    price: 15,
    currency: 'ZAR',
    available: false,
    durationLabel: 'Ready in 24h',
    category: 'add-on',
  },
];

export function mockRequest(path, method) {
  if (method === 'GET' && path.endsWith('/timeslots/dates')) {
    return Promise.resolve(MOCK_DATES);
  }

  if (method === 'GET' && path.includes('/timeslots?')) {
    const date = new URL(path, 'http://mock').searchParams.get('date');
    const fullyBooked = MOCK_DATES.find((d) => d.date === date)?.hasAvailability === false;
    return Promise.resolve(buildSlotsForDate(date, fullyBooked));
  }

  if (method === 'GET' && path.endsWith('/laundry-services')) {
    return Promise.resolve(MOCK_SERVICES);
  }

  if (method === 'GET' && path.includes('/laundry-services/')) {
    const id = path.split('/laundry-services/')[1];
    const service = MOCK_SERVICES.find((s) => s.id === id);
    return service ? Promise.resolve(service) : Promise.reject(new Error('Not found'));
  }

  return Promise.resolve({});
}