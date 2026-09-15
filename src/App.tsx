import React, { useEffect } from 'react';
import { useWallet } from './context/WalletContext';
import { DashboardScreen } from './components/DashboardScreen';
import { SendScreen } from './components/SendScreen';
import { ReceiveScreen } from './components/ReceiveScreen';
import { LightningChannelsScreen } from './components/LightningChannelsScreen';
import { CloudBackupScreen } from './components/CloudBackupScreen';
import { SecurityAuditScreen } from './components/SecurityAuditScreen';
import { PinLockScreen } from './components/PinLockScreen';
import { Navigation } from './components/Navigation';

export const AppContent: React.FC = () => {
  const {
    walletState,
    activeTab,
    setActiveTab,
    unlockWallet,
    unlockWithBiometric,
    lockWallet,
  } = useWallet();

  // Auto-lock on tab hide / blur if enabled in security settings
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.hidden && walletState.securitySettings.isAutoLockEnabled) {
        lockWallet();
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [walletState.securitySettings.isAutoLockEnabled, lockWallet]);

  if (walletState.isLocked) {
    return (
      <PinLockScreen
        onUnlock={unlockWallet}
        onBiometricUnlock={unlockWithBiometric}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[#090C10] text-[#F0F6FC]">
      <main className="animate-in fade-in duration-200">
        {activeTab === 'DASHBOARD' && (
          <DashboardScreen onNavigateTab={setActiveTab} />
        )}
        {activeTab === 'SEND' && (
          <SendScreen onBack={() => setActiveTab('DASHBOARD')} />
        )}
        {activeTab === 'RECEIVE' && (
          <ReceiveScreen onBack={() => setActiveTab('DASHBOARD')} />
        )}
        {activeTab === 'LIGHTNING' && (
          <LightningChannelsScreen onBack={() => setActiveTab('DASHBOARD')} />
        )}
        {activeTab === 'BACKUP' && (
          <CloudBackupScreen onBack={() => setActiveTab('DASHBOARD')} />
        )}
        {activeTab === 'SECURITY' && (
          <SecurityAuditScreen onBack={() => setActiveTab('DASHBOARD')} />
        )}
      </main>

      <Navigation activeTab={activeTab} onTabChange={setActiveTab} />
    </div>
  );
};

export default function App() {
  return <AppContent />;
}
