import React from 'react';
import DashboardLayout from '../../components/common/DashboardLayout.js';
import { PageHeader } from '../../components/common/PageHeader.js';

const WET_ITEMS = ['Fruit & vegetable peels', 'Food leftovers', 'Tea bags & coffee grounds', 'Garden waste', 'Eggshells'];
const DRY_ITEMS = ['Paper & cardboard', 'Plastic bottles & wrappers', 'Glass', 'Metal cans', 'Old clothes & textiles'];
const HAZARDOUS_ITEMS = ['Batteries', 'Medicines & syringes', 'Paint & chemical containers', 'CFL bulbs & electronics'];

export default function SegregationGuide() {
  return (
    <DashboardLayout>
      <PageHeader
        title="Waste Segregation Guide"
        subtitle="Proper segregation earns you reward points and helps keep the city clean."
      />

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <GuideCard title="Wet Waste" emoji="🍃" accent="brand" items={WET_ITEMS} tip="Use a green bin. Collected daily." />
        <GuideCard title="Dry Waste" emoji="📦" accent="blue" items={DRY_ITEMS} tip="Use a blue bin. Keep clean and dry." />
        <GuideCard
          title="Hazardous Waste"
          emoji="☣️"
          accent="red"
          items={HAZARDOUS_ITEMS}
          tip="Hand over separately — never mix with regular waste."
        />
      </div>

      <div className="mt-6 card p-6">
        <h3 className="mb-3 font-semibold text-ink-900">Why segregation matters</h3>
        <ul className="list-disc space-y-2 pl-5 text-sm text-ink-600">
          <li>Properly segregated waste is easier and cheaper to recycle or compost.</li>
          <li>It reduces contamination of recyclable materials.</li>
          <li>It helps sanitation workers handle waste more safely.</li>
          <li>Consistently compliant households earn reward points redeemable for civic benefits.</li>
          <li>Improper segregation may result in a penalty after repeated notices.</li>
        </ul>
      </div>
    </DashboardLayout>
  );
}

function GuideCard({ title, emoji, items, tip, accent }) {
  const accentMap = {
    brand: 'bg-brand-50 text-brand-700 border-brand-200',
    blue: 'bg-blue-50 text-blue-700 border-blue-200',
    red: 'bg-red-50 text-red-700 border-red-200',
  };
  return (
    <div className={`card border-2 p-5 ${accentMap[accent]}`}>
      <div className="mb-3 flex items-center gap-2">
        <span className="text-2xl">{emoji}</span>
        <h3 className="font-bold text-ink-900">{title}</h3>
      </div>
      <ul className="space-y-1.5 text-sm text-ink-700">
        {items.map((item) => (
          <li key={item} className="flex items-start gap-2">
            <span>•</span>
            <span>{item}</span>
          </li>
        ))}
      </ul>
      <p className="mt-4 rounded-lg bg-white/60 p-2.5 text-xs font-medium">{tip}</p>
    </div>
  );
}
