import { Endpoints } from '@/components/endpoints';

export default async function EndpointsPage({ params }: { params: Promise<{ orgId: string; collectionId: string }> }) {
  const { orgId, collectionId } = await params;
  return <Endpoints orgId={orgId} collectionId={collectionId} />;
}
