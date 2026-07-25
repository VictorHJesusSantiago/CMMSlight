export type AssetStatus = 'ACTIVE' | 'INACTIVE' | 'DECOMMISSIONED' | 'UNDER_MAINTENANCE';
export type Criticality = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type CriticalityAlert = 'NONE' | 'WATCH' | 'ALERT' | 'CRITICAL_ALERT';

export interface AssetType {
  id: number;
  name: string;
  description?: string;
  customAttributesSchema?: { name: string; label: string; type: string; required: boolean }[];
}

export interface Asset {
  id: number;
  code: string;
  name: string;
  assetTypeId?: number;
  assetTypeName?: string;
  parentAssetId?: number;
  location?: string;
  manufacturer?: string;
  model?: string;
  serialNumber?: string;
  installDate?: string;
  status: AssetStatus;
  criticality: Criticality;
  criticalityAlert: CriticalityAlert;
  warrantyProvider?: string;
  warrantyExpiration?: string;
  warrantyExpired: boolean;
  estimatedLifespanMonths?: number;
  acquisitionCost?: number;
  acquisitionDate?: string;
  currentDepreciatedValue?: number;
  customAttributes?: Record<string, unknown>;
}

export type WorkOrderType = 'PREVENTIVE' | 'CORRECTIVE' | 'PREDICTIVE';
export type WorkOrderStatus = 'OPEN' | 'SCHEDULED' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
export type WorkOrderPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';

export interface WorkOrder {
  id: number;
  code: string;
  assetId: number;
  assetName: string;
  maintenancePlanId?: number;
  type: WorkOrderType;
  status: WorkOrderStatus;
  priority: WorkOrderPriority;
  title: string;
  description?: string;
  requestedById?: number;
  requestedByName?: string;
  assignedToId?: number;
  assignedToName?: string;
  openedAt: string;
  scheduledAt?: string;
  startedAt?: string;
  completedAt?: string;
  executionMinutes?: number;
  signedByName?: string;
  signedAt?: string;
  reopenedFromId?: number;
}

export interface WorkOrderEvent {
  id: number;
  workOrderId: number;
  eventType: string;
  message: string;
  createdByUserId?: number;
  createdByUserName?: string;
  createdAt: string;
}

export type FrequencyType = 'TIME' | 'USAGE';

export interface MaintenancePlan {
  id: number;
  name: string;
  assetId?: number;
  assetName?: string;
  assetTypeId?: number;
  assetTypeName?: string;
  checklistTemplateId?: number;
  frequencyType: FrequencyType;
  frequencyValue: number;
  frequencyUnit?: string;
  active: boolean;
  lastGeneratedAt?: string;
  nextDueAt?: string;
  overdue: boolean;
}

export interface Part {
  id: number;
  code: string;
  name: string;
  unit: string;
  quantityOnHand: number;
  minQuantity: number;
  belowMinimum: boolean;
  supplierId?: number;
  supplierName?: string;
}

export interface Supplier {
  id: number;
  name: string;
  contactName?: string;
  phone?: string;
  email?: string;
  notes?: string;
}

export interface FailureHistory {
  id: number;
  assetId: number;
  assetName: string;
  workOrderId?: number;
  failedAt: string;
  resolvedAt?: string;
  downtimeMinutes?: number;
  description?: string;
  rootCause?: string;
  classification: string;
  why1?: string;
  why2?: string;
  why3?: string;
  why4?: string;
  why5?: string;
}

export interface AssetReliabilityStats {
  assetId: number;
  assetCode: string;
  assetName: string;
  failureCount: number;
  mtbfHours?: number;
  mttrHours?: number;
}

export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: 'ADMIN' | 'PLANNER' | 'TECHNICIAN' | 'REQUESTER';
  active: boolean;
}
