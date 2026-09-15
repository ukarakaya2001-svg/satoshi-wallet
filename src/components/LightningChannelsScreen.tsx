import React, { useState } from 'react';
import {
  ArrowLeft,
  Zap,
  Copy,
  Check,
  Layers,
  Router,
  Gauge,
  Plus,
  ShieldCheck,
} from 'lucide-react';
import { useWallet } from '../context/WalletContext';
import { CurrencyFormatter } from '../utils/currency';

interface Props {
  onBack: () => void;
}

export const LightningChannelsScreen: React.FC<Props> = ({ onBack }) => {
  const { walletState } = useWallet();
  const [copiedNodeId, setCopiedNodeId] = useState(false);
  const [copiedChannelId, setCopiedChannelId] = useState<string | null>(null);

  const channels = walletState.lightningChannels;
  const totalLocal = channels.reduce((acc, c) => acc + c.localCapacitySats, 0);
  const totalRemote = channels.reduce((acc, c) => acc + c.remoteCapacitySats, 0);
  const totalCapacity = totalLocal + totalRemote;

  const handleCopyNode = () => {
    navigator.clipboard.writeText(walletState.lightningNodeId);
    setCopiedNodeId(true);
    setTimeout(() => setCopiedNodeId(false), 2000);
  };

  const handleCopyChannel = (id: string) => {
    navigator.clipboard.writeText(id);
    setCopiedChannelId(id);
    setTimeout(() => setCopiedChannelId(null), 2000);
  };

  return (
    <div className="min-h-screen bg-[#090C10] pb-28 pt-4 px-4 max-w-xl mx-auto text-white">
      {/* Top Bar */}
      <div className="flex items-center gap-3 mb-4">
        <button
          type="button"
          onClick={onBack}
          className="p-2 rounded-full bg-[#12161F] text-white border border-[#2B3447] hover:bg-[#1B212D]"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-lg font-bold text-white">Lightning Layer-2 Hub</h1>
          <p className="text-[11px] text-[#8B949E]">Anında Eşler Arası Ödeme Kanalları</p>
        </div>
      </div>

      {/* Node ID Card */}
      <div className="p-4 rounded-2xl bg-[#12161F] border border-[#00E5FF]/40 shadow-lg shadow-[#00E5FF]/10 mb-4">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-full bg-[#00E5FF]/15 text-[#00E5FF] flex items-center justify-center">
              <Zap className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-white">Lightning Node Kimliği</p>
              <p className="text-[10px] text-[#00E676] font-semibold">Aktif Eş Yönlendirme Çevrimiçi</p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleCopyNode}
            className="text-xs text-[#00E5FF] hover:underline font-semibold flex items-center gap-1"
          >
            {copiedNodeId ? <Check className="w-3.5 h-3.5 text-[#00E676]" /> : <Copy className="w-3.5 h-3.5" />}
            {copiedNodeId ? 'Kopyalandı' : 'Kopyala'}
          </button>
        </div>
        <p className="font-mono text-[11px] text-white break-all bg-[#090C10] p-2.5 rounded-xl border border-[#2B3447]/60">
          {walletState.lightningNodeId}
        </p>
      </div>

      {/* Liquidity Breakdown Card */}
      <div className="p-5 rounded-2xl bg-gradient-to-b from-[#12161F] to-[#0D1117] border border-[#2B3447] mb-5">
        <div className="flex items-center justify-between text-xs text-[#8B949E] mb-2">
          <span className="font-bold uppercase tracking-wider">Ağ Likidite Dengesi</span>
          <span className="font-mono text-white font-bold">{CurrencyFormatter.formatSats(totalCapacity)}</span>
        </div>

        {/* Capacity Bar */}
        <div className="w-full h-3 rounded-full bg-[#1B212D] overflow-hidden flex mb-3">
          <div
            style={{ width: `${(totalLocal / totalCapacity) * 100}%` }}
            className="bg-[#00E5FF] h-full transition-all"
            title="Yerel Harcama Kapasitesi"
          />
          <div
            style={{ width: `${(totalRemote / totalCapacity) * 100}%` }}
            className="bg-[#6E7681] h-full transition-all"
            title="Uzak Alma Kapasitesi"
          />
        </div>

        <div className="grid grid-cols-2 gap-3 text-xs">
          <div className="p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div className="flex items-center gap-1.5 text-[#00E5FF] font-semibold mb-1">
              <span className="w-2 h-2 rounded-full bg-[#00E5FF]" />
              <span>Giden (Harcama)</span>
            </div>
            <p className="text-sm font-bold font-mono text-white">{CurrencyFormatter.formatSats(totalLocal)}</p>
            <p className="text-[10px] text-[#8B949E]">Yerel Likidite</p>
          </div>

          <div className="p-3 rounded-xl bg-[#090C10] border border-[#2B3447]/60">
            <div className="flex items-center gap-1.5 text-[#8B949E] font-semibold mb-1">
              <span className="w-2 h-2 rounded-full bg-[#6E7681]" />
              <span>Gelen (Alma)</span>
            </div>
            <p className="text-sm font-bold font-mono text-white">{CurrencyFormatter.formatSats(totalRemote)}</p>
            <p className="text-[10px] text-[#8B949E]">Uzak Düğüm Likiditesi</p>
          </div>
        </div>
      </div>

      {/* Active Channels List */}
      <div className="space-y-3">
        <div className="flex items-center justify-between mb-1">
          <h2 className="text-xs font-bold text-[#8B949E] uppercase tracking-wider">
            Aktif Kanallar ({channels.length})
          </h2>
          <span className="text-[11px] text-[#00E5FF] font-medium flex items-center gap-1">
            <ShieldCheck className="w-3.5 h-3.5 text-[#00E676]" />
            BOLT-2 Rezerv Korumalı
          </span>
        </div>

        {channels.map((ch) => {
          const chTotal = ch.localCapacitySats + ch.remoteCapacitySats;
          const localPct = (ch.localCapacitySats / chTotal) * 100;

          return (
            <div
              key={ch.channelId}
              className="p-4 rounded-2xl bg-[#12161F] border border-[#2B3447] space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5">
                  <div className="w-8 h-8 rounded-xl bg-[#00E5FF]/10 text-[#00E5FF] flex items-center justify-center">
                    <Router className="w-4 h-4" />
                  </div>
                  <div>
                    <h3 className="text-xs font-bold text-white">{ch.remoteNodeAlias}</h3>
                    <p className="text-[10px] text-[#8B949E] font-mono">{ch.channelId}</p>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-[#00E676]/15 text-[#00E676] font-bold">
                    {ch.status}
                  </span>
                  <button
                    type="button"
                    onClick={() => handleCopyChannel(ch.channelId)}
                    className="p-1 rounded text-[#8B949E] hover:text-white"
                  >
                    {copiedChannelId === ch.channelId ? (
                      <Check className="w-3.5 h-3.5 text-[#00E676]" />
                    ) : (
                      <Copy className="w-3.5 h-3.5" />
                    )}
                  </button>
                </div>
              </div>

              {/* Progress bar */}
              <div className="space-y-1">
                <div className="w-full h-2 rounded-full bg-[#090C10] overflow-hidden flex">
                  <div style={{ width: `${localPct}%` }} className="bg-[#00E5FF] h-full" />
                  <div style={{ width: `${100 - localPct}%` }} className="bg-[#2B3447] h-full" />
                </div>
                <div className="flex justify-between text-[10px] font-mono text-[#8B949E]">
                  <span className="text-[#00E5FF]">Yerel: {ch.localCapacitySats.toLocaleString()} sats</span>
                  <span>Uzak: {ch.remoteCapacitySats.toLocaleString()} sats</span>
                </div>
              </div>

              <div className="flex items-center justify-between pt-1 border-t border-[#2B3447]/50 text-[11px] text-[#8B949E]">
                <span>Aktif HTLC Yönlendirme: {ch.htlcCount} işlem</span>
                <span className="text-white font-mono font-medium">Kapasite: {chTotal.toLocaleString()} sats</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
