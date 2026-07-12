import React from 'react';
import { STATUS_BADGE_STYLES } from '../../utils/constants.js';

export default function Badge({ status, children }) {
  const style = STATUS_BADGE_STYLES[status] || 'bg-ink-100 text-ink-600';
  return <span className={`badge ${style}`}>{children || status}</span>;
}
