export interface ApiError {
  status: number;
  errorCode: string;
  message: string;
  path?: string;
  raw?: any;
}