import { Collections } from '@/components/collections';

export default async function CollectionsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = await params;
  return <Collections orgId={orgId} />;
}
