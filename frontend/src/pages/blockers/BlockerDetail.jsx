import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { blockerService } from '../../services/blockerService';
import { solutionService } from '../../services/solutionService';
import { commentService } from '../../services/commentService';
import { useAuth } from '../../context/AuthContext';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Input';
import { SolutionList } from '../../components/solutions/SolutionList';
import { CommentList } from '../../components/comments/CommentList';
import { STATUS_COLORS, SEVERITY_COLORS } from '../../utils/constants';
import { formatDateTime } from '../../utils/format';
import { ArrowLeft, Edit, CheckCircle, Upload, X, Image as ImageIcon, Video } from 'lucide-react';
import toast from 'react-hot-toast';

export const BlockerDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [blocker, setBlocker] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showSolutionForm, setShowSolutionForm] = useState(false);
  const [solutionContent, setSolutionContent] = useState('');
  const [submittingSolution, setSubmittingSolution] = useState(false);
  const [selectedSolutionFiles, setSelectedSolutionFiles] = useState([]);
  const [uploadedSolutionFileUrls, setUploadedSolutionFileUrls] = useState([]);
  const [uploadingSolutionFiles, setUploadingSolutionFiles] = useState(false);

  useEffect(() => {
    fetchBlocker();
  }, [id]);

  const fetchBlocker = async () => {
    try {
      setLoading(true);
      const data = await blockerService.getBlocker(id);
      console.log('Fetched blocker:', data);
      console.log('Blocker mediaUrls:', data.mediaUrls);
      console.log('MediaUrls length:', data.mediaUrls?.length);
      setBlocker(data);
    } catch (error) {
      toast.error('Failed to fetch blocker');
      console.error(error);
      navigate('/blockers');
    } finally {
      setLoading(false);
    }
  };

  const handleSolutionFileSelect = (e) => {
    const files = Array.from(e.target.files);
    setSelectedSolutionFiles((prev) => [...prev, ...files]);
  };

  const handleRemoveSolutionFile = (index) => {
    setSelectedSolutionFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleUploadSolutionFiles = async () => {
    if (selectedSolutionFiles.length === 0) return;

    setUploadingSolutionFiles(true);
    try {
      console.log('Uploading solution files:', selectedSolutionFiles.map(f => f.name));
      const response = await solutionService.uploadFiles(selectedSolutionFiles);
      console.log('File upload response:', response);
      console.log('Received fileUrls:', response.fileUrls);
      setUploadedSolutionFileUrls((prev) => {
        const newUrls = [...prev, ...response.fileUrls];
        console.log('Updated uploadedSolutionFileUrls:', newUrls);
        return newUrls;
      });
      setSelectedSolutionFiles([]);
      toast.success('Files uploaded successfully!');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to upload files');
      console.error('Error uploading solution files:', error);
    } finally {
      setUploadingSolutionFiles(false);
    }
  };

  const handleRemoveUploadedSolutionFile = (index) => {
    setUploadedSolutionFileUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const isImage = (url) => {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  };

  const isVideo = (url) => {
    return /\.(mp4|webm|mov|avi)$/i.test(url);
  };

  const handleSubmitSolution = async (e) => {
    e.preventDefault();
    if (!user?.userId) {
      toast.error('Please login to submit a solution');
      return;
    }

    setSubmittingSolution(true);
    try {
      // Upload any selected files that haven't been uploaded yet
      let finalMediaUrls = [...uploadedSolutionFileUrls];
      
      if (selectedSolutionFiles.length > 0) {
        console.log('Auto-uploading solution files before submit:', selectedSolutionFiles.map(f => f.name));
        try {
          const uploadResponse = await solutionService.uploadFiles(selectedSolutionFiles);
          console.log('Auto-upload response:', uploadResponse);
          if (uploadResponse?.fileUrls && uploadResponse.fileUrls.length > 0) {
            finalMediaUrls = [...finalMediaUrls, ...uploadResponse.fileUrls];
            console.log('Final mediaUrls after auto-upload:', finalMediaUrls);
          }
        } catch (uploadError) {
          console.error('Error auto-uploading files:', uploadError);
          toast.error('Failed to upload files. Please try uploading them manually.');
          return;
        }
      }
      
      console.log('Submitting solution with mediaUrls:', finalMediaUrls);
      const result = await solutionService.addSolution(id, solutionContent, user.userId, finalMediaUrls);
      console.log('Solution created:', result);
      console.log('Solution mediaUrls in response:', result.mediaUrls);
      toast.success('Solution added successfully!');
      setSolutionContent('');
      setUploadedSolutionFileUrls([]);
      setSelectedSolutionFiles([]);
      setShowSolutionForm(false);
      fetchBlocker(); // Refresh to show new solution
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add solution');
      console.error('Error creating solution:', error);
    } finally {
      setSubmittingSolution(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (!blocker) {
    return null;
  }

  return (
    <div className="space-y-6">
      <Button variant="secondary" onClick={() => navigate('/blockers')}>
        <ArrowLeft className="w-4 h-4" />
        Back to Blockers
      </Button>

      {/* Blocker Details */}
      <Card>
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{blocker.title}</h1>
              <div className="flex items-center gap-3 mt-2">
                <Badge
                  variant={blocker.status === 'OPEN' ? 'primary' : blocker.status === 'RESOLVED' ? 'success' : 'default'}
                >
                  {blocker.status}
                </Badge>
                <Badge variant="warning">{blocker.severity}</Badge>
                {blocker.tags?.map((tag) => (
                  <Badge key={tag} variant="default">
                    {tag}
                  </Badge>
                ))}
              </div>
            </div>
            {blocker.status !== 'RESOLVED' && user?.userId === blocker.createdBy && (
              <Button variant="secondary">
                <Edit className="w-4 h-4" />
                Edit
              </Button>
            )}
          </div>
        </CardHeader>
        <CardBody>
          <div className="space-y-4">
            <div>
              <h3 className="text-sm font-medium text-gray-500 mb-1">Description</h3>
              <p className="text-gray-900 whitespace-pre-wrap">{blocker.description}</p>
            </div>
            
            {/* Blocker Media */}
            {blocker.mediaUrls && blocker.mediaUrls.length > 0 ? (
              <div>
                <h3 className="text-sm font-medium text-gray-500 mb-2">Media</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                  {blocker.mediaUrls.map((url, index) => {
                    // Extract fileId (UUID) from URL
                    const fileId = url.includes('/') ? url.split('/').pop() : url;
                    const fullUrl = url.startsWith('http') ? url : blockerService.getFileUrl(fileId);
                    
                    // Try to determine type from URL or use generic media display
                    // Since we're using UUIDs, we'll try both image and video and let the browser handle it
                    return (
                      <div key={index} className="relative">
                        <img
                          src={fullUrl}
                          alt={`Blocker media ${index + 1}`}
                          className="w-full h-48 object-cover rounded-lg border border-gray-200"
                          onError={(e) => {
                            // If image fails, try as video
                            const video = document.createElement('video');
                            video.src = fullUrl;
                            video.className = "w-full h-48 object-cover rounded-lg border border-gray-200";
                            video.controls = true;
                            video.onerror = () => {
                              console.error(`Failed to load blocker media: ${fullUrl}`);
                              e.target.parentElement.innerHTML = `
                                <div class="w-full h-48 bg-gray-100 rounded-lg border border-gray-200 flex items-center justify-center">
                                  <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                                  </svg>
                                </div>
                              `;
                            };
                            e.target.parentElement.replaceChild(video, e.target);
                          }}
                          onLoad={() => {
                            console.log(`Successfully loaded blocker image: ${fullUrl}`);
                          }}
                        />
                      </div>
                    );
                  })}
                </div>
              </div>
            ) : null}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
              <div>
                <p className="text-sm text-gray-500">Created</p>
                <p className="text-sm font-medium text-gray-900">
                  {formatDateTime(blocker.createdAt)}
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500">Created By</p>
                <p className="text-sm font-medium text-gray-900">
                  {blocker.createdBy || 'Unknown'}
                </p>
              </div>
              {blocker.assignedTo && (
                <div>
                  <p className="text-sm text-gray-500">Assigned To</p>
                  <p className="text-sm font-medium text-gray-900">{blocker.assignedTo}</p>
                </div>
              )}
              {blocker.bestSolutionId && (
                <div>
                  <p className="text-sm text-gray-500">Best Solution</p>
                  <p className="text-sm font-medium text-success-600">
                    <CheckCircle className="w-4 h-4 inline mr-1" />
                    Selected
                  </p>
                </div>
              )}
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Solutions Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-semibold">Solutions</h2>
            {!showSolutionForm && blocker.status !== 'RESOLVED' && (
              <Button onClick={() => setShowSolutionForm(true)}>
                Add Solution
              </Button>
            )}
          </div>
        </CardHeader>
        <CardBody>
          {showSolutionForm && (
            <form onSubmit={handleSubmitSolution} className="mb-6 pb-6 border-b border-gray-200 space-y-4">
              <Textarea
                label="Your Solution"
                value={solutionContent}
                onChange={(e) => setSolutionContent(e.target.value)}
                required
                rows={6}
                placeholder="Describe your solution to this blocker..."
              />
              
              {/* File Upload Section for Solution */}
              <div className="space-y-4">
                <label className="block text-sm font-medium text-gray-700">
                  Photos & Videos (Optional)
                </label>
                
                {/* File Input */}
                <div className="flex items-center gap-4">
                  <label className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg cursor-pointer hover:bg-gray-50 transition-colors">
                    <Upload className="w-5 h-5" />
                    <span className="text-sm font-medium">Choose Files</span>
                    <input
                      type="file"
                      multiple
                      accept="image/*,video/*"
                      onChange={handleSolutionFileSelect}
                      className="hidden"
                    />
                  </label>
                  {selectedSolutionFiles.length > 0 && (
                    <Button
                      type="button"
                      onClick={handleUploadSolutionFiles}
                      loading={uploadingSolutionFiles}
                      variant="secondary"
                    >
                      Upload {selectedSolutionFiles.length} file(s)
                    </Button>
                  )}
                </div>

                {/* Selected Files Preview */}
                {selectedSolutionFiles.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {selectedSolutionFiles.map((file, index) => (
                      <div
                        key={index}
                        className="flex items-center gap-2 px-3 py-1 bg-gray-100 rounded-lg text-sm"
                      >
                        <span className="truncate max-w-[200px]">{file.name}</span>
                        <button
                          type="button"
                          onClick={() => handleRemoveSolutionFile(index)}
                          className="text-red-600 hover:text-red-800"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Uploaded Files Preview */}
                {uploadedSolutionFileUrls.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-sm text-gray-600">Uploaded files:</p>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                      {uploadedSolutionFileUrls.map((url, index) => {
                        const fullUrl = url.startsWith('http') ? url : solutionService.getFileUrl(url.split('/').pop());
                        return (
                          <div key={index} className="relative group">
                            {isImage(url) ? (
                              <img
                                src={fullUrl}
                                alt={`Upload ${index + 1}`}
                                className="w-full h-32 object-cover rounded-lg border border-gray-200"
                              />
                            ) : isVideo(url) ? (
                              <video
                                src={fullUrl}
                                className="w-full h-32 object-cover rounded-lg border border-gray-200"
                                controls
                              />
                            ) : (
                              <div className="w-full h-32 bg-gray-100 rounded-lg border border-gray-200 flex items-center justify-center">
                                <ImageIcon className="w-8 h-8 text-gray-400" />
                              </div>
                            )}
                            <button
                              type="button"
                              onClick={() => handleRemoveUploadedSolutionFile(index)}
                              className="absolute top-2 right-2 bg-red-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>

              <div className="flex gap-4 mt-4">
                <Button type="submit" loading={submittingSolution}>
                  Submit Solution
                </Button>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setShowSolutionForm(false);
                    setSolutionContent('');
                    setUploadedSolutionFileUrls([]);
                    setSelectedSolutionFiles([]);
                  }}
                >
                  Cancel
                </Button>
              </div>
            </form>
          )}
          <SolutionList blockerId={id} blocker={blocker} onUpdate={fetchBlocker} />
        </CardBody>
      </Card>

      {/* Comments Section */}
      <Card>
        <CardHeader>
          <h2 className="text-2xl font-semibold">Comments</h2>
        </CardHeader>
        <CardBody>
          <CommentList blockerId={id} />
        </CardBody>
      </Card>
    </div>
  );
};

