import React from 'react';
import {
  Wallet,
  ArrowUpRight,
  ArrowDownLeft,
  Zap,
  Cloud,
  ShieldCheck,
} from 'lucide-react';
import { WalletNavTab } from '../types';

interface Props {
  activeTab: WalletNavTab;
  onTabChange: (tab: WalletNavTab) => void;
}

export const Navigation: React.FC<Props> = ({ activeTab, onTabChange }) => {
  const tabs: { id: WalletNavTab; label: string; icon: React.FC<{ className?: string }> }[] = [
    { id: 'DASHBOARD', label: 'Cüzdan', icon: Wallet },
    { id: 'SEND', label: 'Gönder', icon: ArrowUpRight },
    { id: 'RECEIVE', label: 'Al', icon: ArrowDownLeft },
    { id: 'LIGHTNING', label: 'Lightning', icon: Zap },
    { id: 'BACKUP', label: 'Yedekle', icon: Cloud },
    { id: 'SECURITY', label: 'Güvenlik', icon: ShieldCheck },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#090C10]/95 backdrop-blur-md border-t border-[#2B3447]/60 pb-safe">
      <div className="max-w-xl mx-auto px-2 py-2 flex items-center justify-around">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;

          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => onTabChange(tab.id)}
              className={`flex flex-col items-center justify-center py-1.5 px-3 rounded-xl transition-all ${
                isActive
                  ? 'text-[#F7931A] font-bold scale-105'
                  : 'text-[#8B949E] hover:text-[#F0F6FC] font-medium'
              }`}
            >
              <div
                className={`p-1 rounded-lg transition-colors ${
                  isActive ? 'bg-[#F7931A]/15 text-[#F7931A]' : ''
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] mt-0.5 tracking-tight">{tab.label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
