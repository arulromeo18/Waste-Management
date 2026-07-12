export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  WORKER: 'WORKER',
  CITIZEN: 'CITIZEN',
};

export const COMPLAINT_STATUSES = ['PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED'];

export const COMPLAINT_CATEGORIES = [
  'Missed Pickup',
  'Improper Collection',
  'Worker Conduct',
  'Overflowing Bin',
  'Illegal Dumping',
  'Other',
];

export const DAYS_OF_WEEK = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export const VEHICLE_TYPES = ['Compactor Truck', 'Mini Truck', 'Tricycle', 'Tipper', 'Other'];

export const STATUS_BADGE_STYLES = {
  PENDING: 'bg-amber-100 text-amber-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  RESOLVED: 'bg-brand-100 text-brand-700',
  REJECTED: 'bg-red-100 text-red-700',
  ACTIVE: 'bg-brand-100 text-brand-700',
  INACTIVE: 'bg-ink-100 text-ink-500',
  WAIVED: 'bg-ink-100 text-ink-500',
  SETTLED: 'bg-brand-100 text-brand-700',
};
