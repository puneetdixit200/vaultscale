import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'VaultScale',
  description: 'A secure workspace for team API collections and endpoint execution.',
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
